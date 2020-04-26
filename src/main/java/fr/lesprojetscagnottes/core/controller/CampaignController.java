package fr.lesprojetscagnottes.core.controller;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.entity.model.DonationModel;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.pagination.DataPage;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.security.UserPrincipal;
import fr.lesprojetscagnottes.core.service.SlackClientService;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class CampaignController {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignController.class);

    @Autowired
    private Gson gson;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit", "filter"})
    public Page<Campaign> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit, @RequestParam("filter") List<String> filters) {
        Pageable pageable = PageRequest.of(offset, limit);
        Set<CampaignStatus> statuses = new LinkedHashSet<>();
        for(String filter : filters) {
            statuses.add(CampaignStatus.valueOf(filter));
        }
        if(statuses.isEmpty()) {
            statuses.addAll(List.of(CampaignStatus.values()));
        }
        return campaignRepository.findAllByStatusInOrderByStatusAscFundingDeadlineAsc(statuses, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"memberId"})
    public Set<Campaign> getByMemberId(@RequestParam("memberId") Long memberId) {
        Set<Campaign> projectsByLeader = campaignRepository.findAllByLeaderId(memberId);
        Set<Campaign> projectsByPeopleGivingTime = campaignRepository.findAllByPeopleGivingTime_Id(memberId);
        projectsByLeader.addAll(projectsByPeopleGivingTime);
        return projectsByLeader;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Campaign findById(@PathVariable("id") Long id) {
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if (campaign == null) {
            throw new NotFoundException();
        } else {
            return campaign;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Organization> findByIdOrganizations(@PathVariable("id") Long id) {
        Campaign campaign = findById(id);
        return campaign.getOrganizations();
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId", "offset", "limit"})
    public Page<Campaign> getByBudgetId(@RequestParam("budgetId") long budgetId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return campaignRepository.findByBudgets_idOrderByIdDesc(budgetId, pageable);
    }

    @Operation(summary = "Get donations made on a project", description = "Get donations made on a project", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<DonationModel> getDonations(Principal principal, @PathVariable("id") long projectId, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        // Fails if project ID is missing
        if(projectId <= 0) {
            LOGGER.error("Impossible to get donations by project ID : Project ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the project
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findAllProjectsByUserInOrganizations(userLoggedInId, projectId).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Campaign campaign = campaignRepository.findById(projectId).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            LOGGER.error("Impossible to get donations by project ID : project {} not found", projectId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Page<Donation> entities = donationRepository.findByCampaign_idOrderByIdAsc(projectId, PageRequest.of(offset, limit, Sort.by("id")));
        DataPage<DonationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(DonationModel.fromEntity(entity)));
        return models;
    }


    @RequestMapping(value = "/api/project", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public Campaign create(Principal principal, @RequestBody Campaign campaign) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        final User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());

        Set<Organization> organizations = campaign.getOrganizations();
        Set<Organization> newOrganizations = new LinkedHashSet<>();
        organizations.forEach(organization -> {
            Organization organizationInDb = organizationRepository.findById(organization.getId()).orElse(null);
            if (organizationInDb == null) {
                throw new NotFoundException();
            } else {
                organizationInDb.addProject(campaign);
                newOrganizations.add(organizationInDb);
            }
        });
        campaign.setOrganizations(newOrganizations);

        Set<Budget> budgets = campaign.getBudgets();
        budgets.forEach(budget -> {
            Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
            if (budgetInDb == null) {
                throw new NotFoundException();
            } else {
                budgetInDb.getCampaigns().add(campaign);
                budget = budgetInDb;
            }
        });
        campaign.setBudgets(budgets);

        Campaign p = campaignRepository.save(campaign);
        User leader = p.getLeader();

        String defaultUser = new StringBuilder("*")
                .append(userLoggedIn.getFirstname())
                .append(" ")
                .append(userLoggedIn.getLastname())
                .append("*")
                .toString();

        String endMessage = new StringBuilder(" vient de créer le projet cagnotte *")
                .append(p.getTitle())
                .append("* et vous êtes tous invités à y participer !")
                .append("\nDécouvrez le vite sur ")
                .append(WEB_URL)
                .append("/projects/")
                .append(p.getId())
                .toString();

        newOrganizations.forEach(organization -> {
            if(organization.getSlackTeam() != null) {
                StringBuilder stringBuilderUser = new StringBuilder(":rocket: ");
                organization.getMembers().stream()
                        .filter(member -> member.getId() == leader.getId())
                        .findAny()
                        .ifPresentOrElse(member -> {
                            organization.getSlackTeam().getSlackUsers().stream()
                                    .filter(slackUser -> slackUser.getUser().getId() == leader.getId())
                                    .findAny()
                                    .ifPresentOrElse(slackUser -> {
                                        stringBuilderUser.append("<@")
                                                .append(slackUser.getSlackId())
                                                .append(">");
                                    }, () -> {
                                        stringBuilderUser.append(defaultUser);
                                    });

                            stringBuilderUser.append(endMessage);
                        },() -> {
                            stringBuilderUser.append(defaultUser)
                                    .append(endMessage);
                        });
                LOGGER.info("[ProjectController][create][" + campaign.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + stringBuilderUser.toString());
                String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                slackClientService.postMessage(organization.getSlackTeam(), channelId, stringBuilderUser.toString());
                LOGGER.info("[ProjectController][create][" + campaign.getId() + "] Slack Message Sent");
            }
        });

        return p;
    }

    @RequestMapping(value = "/api/project", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public Campaign update(@RequestBody Campaign campaign) {
        Campaign campaignInDb = campaignRepository.findById(campaign.getId()).orElse(null);
        if (campaignInDb == null) {
            throw new NotFoundException();
        } else {
            Set<Organization> organizations = campaign.getOrganizations();
            organizations.forEach(organization -> {
                Organization organizationInDb = organizationRepository.findById(organization.getId()).orElse(null);
                if (organizationInDb == null) {
                    throw new NotFoundException();
                } else {
                    if (organizationInDb.getCampaigns().stream().noneMatch(prj -> campaignInDb.getId().equals(prj.getId()))) {
                        organizationInDb.addProject(campaign);
                    }
                    organization = organizationInDb;
                }
            });
            campaignInDb.setOrganizations(organizations);

            Set<Budget> budgets = campaign.getBudgets();
            budgets.forEach(budget -> {
                Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
                if (budgetInDb == null) {
                    throw new NotFoundException();
                } else {
                    if (budgetInDb.getCampaigns().stream().noneMatch(prj -> campaignInDb.getId().equals(prj.getId()))) {
                        budgetInDb.getCampaigns().add(campaign);
                    }
                    budget = budgetInDb;
                }
            });
            campaign.setBudgets(budgets);

            campaignInDb.setTitle(campaign.getTitle());
            campaignInDb.setShortDescription(campaign.getShortDescription());
            campaignInDb.setLongDescription(campaign.getLongDescription());
            campaignInDb.setLeader(campaign.getLeader());
            campaignInDb.setPeopleRequired(campaign.getPeopleRequired());
            if (campaign.getDonationsRequired() > campaignInDb.getDonationsRequired()) {
                campaignInDb.setDonationsRequired(campaign.getDonationsRequired());
            }
            return campaignRepository.save(campaignInDb);
        }
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void save(@PathVariable("id") String id, @RequestBody Campaign campaign) {
        Campaign campaignInDb = campaignRepository.findById(Long.valueOf(id)).orElse(null);
        if (campaignInDb == null) {
            throw new NotFoundException();
        } else {
            campaignRepository.save(campaign);
        }
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") String id) {
        campaignRepository.deleteById(Long.valueOf(id));
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/join", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Campaign join(@PathVariable("id") Long id, Principal principal) {
        Campaign campaignInDb = campaignRepository.findById(id).orElse(null);
        if (campaignInDb == null) {
            throw new NotFoundException();
        } else {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
            final User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());
            User userInPeopleGivingTime = campaignInDb.getPeopleGivingTime().stream().filter(userGivingTime -> userLoggedIn.getId().equals(userGivingTime.getId())).findFirst().orElse(null);
            if (userInPeopleGivingTime == null) {
                campaignInDb.addPeopleGivingTime(userLoggedIn);
            } else {
                campaignInDb.getPeopleGivingTime().remove(userInPeopleGivingTime);
            }
            return campaignRepository.save(campaignInDb);
        }
    }

    @RequestMapping(value = "/api/project/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void validate() {
        processProjectFundingDeadlines();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void processProjectFundingDeadlines() {
        LOGGER.info("[processProjectFundingDeadlines] Start Project Funding Deadlines Processing");
        Set<Campaign> campaigns = campaignRepository.findAllByStatusAndFundingDeadlineLessThan(CampaignStatus.A_IN_PROGRESS, new Date());
        LOGGER.info("[processProjectFundingDeadlines] " + campaigns.size() + " project(s) found");
        campaigns.forEach(project -> {
            Set<Donation> donations = donationRepository.findAllByCampaignId(project.getId());
            float totalDonations = 0f;
            for (Donation donation : donations) {
                totalDonations += donation.getAmount();
            }
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Project : " + project.getTitle());
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Teammates : " + project.getPeopleGivingTime().size() + " / " + project.getPeopleRequired());
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Donations : " + totalDonations + " € / " + project.getDonationsRequired() + " €");
            if (totalDonations >= project.getDonationsRequired()
                    && project.getPeopleGivingTime().size() >= project.getPeopleRequired()) {
                project.setStatus(CampaignStatus.B_READY);
                LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Status => B_READY");
            } else {
                project.setStatus(CampaignStatus.C_AVORTED);
                LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Status => C_AVORTED");
                donationRepository.deleteByCampaignId(project.getId());
                LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Donations deleted");
            }
        });
        LOGGER.info("[processProjectFundingDeadlines] End Project Funding Deadlines Processing");
    }

    @RequestMapping(value = "/api/project/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void notifyProjectsAlmostFinished(Principal principal) {
        notifyProjectsAlmostFinished();
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notifyProjectsAlmostFinished() {
        LOGGER.info("[notifyProjectsAlmostFinished] Start Notify Project Almost Finished");
        Set<Campaign> campaigns = campaignRepository.findAllByStatus(CampaignStatus.A_IN_PROGRESS);
        LOGGER.info("[notifyProjectsAlmostFinished] " + campaigns.size() + " project(s) found");
        campaigns.forEach(project -> {
            long diffInMillies = Math.abs(project.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            LOGGER.info("[notifyProjectsAlmostFinished][" + project.getId() + "] Project : " + project.getTitle());
            LOGGER.info("[notifyProjectsAlmostFinished][" + project.getId() + "] Days until deadline : " + diff);

            if(diff == 7 || diff == 1) {
                notifyProjectStatus(project, diff);
            }
        });
        LOGGER.info("[notifyProjectsAlmostFinished] End Notify Project Almost Finished");
    }

    @RequestMapping(value = "/api/project/{id}/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void notifyProjectStatus(@PathVariable("id") long id) {
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if(campaign == null) {
            throw new NotFoundException();
        } else {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
            notifyProjectStatus(campaign, diff);
        }
    }

    public void notifyProjectStatus(Campaign campaign, long daysUntilDeadline) {

        int teamMatesMissing = campaign.getPeopleRequired() - campaign.getPeopleGivingTime().size();
        LOGGER.info("[notifyProjectStatus][" + campaign.getId() + "] Teammates missing : " + teamMatesMissing);

        Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
        float totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        float donationsMissing = campaign.getDonationsRequired() - totalDonations;
        LOGGER.info("[notifyProjectsAlmostFinished][" + campaign.getId() + "] Donations : " + donationsMissing + " €");

        if(teamMatesMissing > 0 || donationsMissing > 0) {

            User leader = campaign.getLeader();

            String defaultUser = new StringBuilder("*")
                    .append(leader.getFirstname())
                    .append(" ")
                    .append(leader.getLastname())
                    .append("*")
                    .toString();

            StringBuilder endMessage = new StringBuilder(" a besoin de vous !\n")
                    .append("Il reste *")
                    .append(daysUntilDeadline)
                    .append(" jour(s)* pour compléter la campagne du projet *")
                    .append(campaign.getTitle())
                    .append("* !\n\nA ce jour, il manque :\n");

            if(teamMatesMissing > 0) {
                endMessage.append(" - ")
                        .append(teamMatesMissing)
                        .append(" personne(s) dans l'équipe\n");
            }
            if(donationsMissing > 0) {
                endMessage.append(" - ")
                        .append(String.format("%.2f", donationsMissing))
                        .append(" € de budget\n");
            }

            campaign.getOrganizations().forEach(organization -> {
                if(organization.getSlackTeam() != null) {
                    StringBuilder stringBuilderUser = new StringBuilder(":timer_clock: ");
                    organization.getMembers().stream()
                            .filter(member -> member.getId() == leader.getId())
                            .findAny()
                            .ifPresentOrElse(member -> {
                                organization.getSlackTeam().getSlackUsers().stream()
                                        .filter(slackUser -> slackUser.getUser().getId() == leader.getId())
                                        .findAny()
                                        .ifPresentOrElse(slackUser -> {
                                            stringBuilderUser.append("<@")
                                                    .append(slackUser.getSlackId())
                                                    .append(">");
                                        }, () -> {
                                            stringBuilderUser.append(defaultUser);
                                        });

                                stringBuilderUser.append(endMessage.toString())
                                        .append("\nRendez-vous à l'adresse suivante pour participer : \n")
                                        .append(WEB_URL)
                                        .append("/projects/")
                                        .append(campaign.getId());
                            },() -> {
                                stringBuilderUser.append(defaultUser)
                                        .append(endMessage.toString())
                                        .append("\nRendez-vous à l'adresse suivante pour participer : \n")
                                        .append(WEB_URL)
                                        .append("/projects/")
                                        .append(campaign.getId());
                            });
                    LOGGER.info("[notifyProjectStatus][" + campaign.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + stringBuilderUser.toString());
                    String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                    slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                    slackClientService.postMessage(organization.getSlackTeam(), channelId, stringBuilderUser.toString());
                    LOGGER.info("[notifyProjectStatus][" + campaign.getId() + "] Slack Message Sent");
                }
            });

        }
    }
}
