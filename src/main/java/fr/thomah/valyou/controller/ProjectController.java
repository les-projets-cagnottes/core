package fr.thomah.valyou.controller;

import com.google.gson.Gson;
import fr.thomah.valyou.entity.model.DonationModel;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.pagination.DataPage;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.security.UserPrincipal;
import fr.thomah.valyou.service.SlackClientService;
import fr.thomah.valyou.service.UserService;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class ProjectController {

    private static final String WEB_URL = System.getenv("VALYOU_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

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
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit", "filter"})
    public Page<Project> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit, @RequestParam("filter") List<String> filters) {
        Pageable pageable = PageRequest.of(offset, limit);
        Set<ProjectStatus> statuses = new LinkedHashSet<>();
        for(String filter : filters) {
            statuses.add(ProjectStatus.valueOf(filter));
        }
        if(statuses.isEmpty()) {
            statuses.addAll(List.of(ProjectStatus.values()));
        }
        return projectRepository.findAllByStatusInOrderByStatusAscFundingDeadlineAsc(statuses, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"memberId"})
    public Set<Project> getByMemberId(@RequestParam("memberId") Long memberId) {
        Set<Project> projectsByLeader = projectRepository.findAllByLeaderId(memberId);
        Set<Project> projectsByPeopleGivingTime = projectRepository.findAllByPeopleGivingTime_Id(memberId);
        projectsByLeader.addAll(projectsByPeopleGivingTime);
        return projectsByLeader;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Project findById(@PathVariable("id") Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new NotFoundException();
        } else {
            return project;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Organization> findByIdOrganizations(@PathVariable("id") Long id) {
        Project project = findById(id);
        return project.getOrganizations();
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId", "offset", "limit"})
    public Page<Project> getByBudgetId(@RequestParam("budgetId") long budgetId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return projectRepository.findByBudgets_idOrderByIdDesc(budgetId, pageable);
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
        if(projectRepository.findAllProjectsByUserInOrganizations(userLoggedInId, projectId).isEmpty() && !userService.isAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(projectId).orElse(null);

        // Verify that any of references are not null
        if(project == null) {
            LOGGER.error("Impossible to get donations by project ID : project {} not found", projectId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Page<Donation> entities = donationRepository.findByProject_idOrderByIdAsc(projectId, PageRequest.of(offset, limit, Sort.by("id")));
        DataPage<DonationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(DonationModel.fromEntity(entity)));
        return models;
    }


    @RequestMapping(value = "/api/project", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public Project create(Principal principal, @RequestBody Project project) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        final User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());

        Set<Organization> organizations = project.getOrganizations();
        Set<Organization> newOrganizations = new LinkedHashSet<>();
        organizations.forEach(organization -> {
            Organization organizationInDb = organizationRepository.findById(organization.getId()).orElse(null);
            if (organizationInDb == null) {
                throw new NotFoundException();
            } else {
                organizationInDb.addProject(project);
                newOrganizations.add(organizationInDb);
            }
        });
        project.setOrganizations(newOrganizations);

        Set<Budget> budgets = project.getBudgets();
        budgets.forEach(budget -> {
            Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
            if (budgetInDb == null) {
                throw new NotFoundException();
            } else {
                budgetInDb.getProjects().add(project);
                budget = budgetInDb;
            }
        });
        project.setBudgets(budgets);

        Project p = projectRepository.save(project);
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
                LOGGER.info("[ProjectController][create][" + project.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + stringBuilderUser.toString());
                String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                slackClientService.postMessage(organization.getSlackTeam(), channelId, stringBuilderUser.toString());
                LOGGER.info("[ProjectController][create][" + project.getId() + "] Slack Message Sent");
            }
        });

        return p;
    }

    @RequestMapping(value = "/api/project", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public Project update(@RequestBody Project project) {
        Project projectInDb = projectRepository.findById(project.getId()).orElse(null);
        if (projectInDb == null) {
            throw new NotFoundException();
        } else {
            Set<Organization> organizations = project.getOrganizations();
            organizations.forEach(organization -> {
                Organization organizationInDb = organizationRepository.findById(organization.getId()).orElse(null);
                if (organizationInDb == null) {
                    throw new NotFoundException();
                } else {
                    if (organizationInDb.getProjects().stream().noneMatch(prj -> projectInDb.getId().equals(prj.getId()))) {
                        organizationInDb.addProject(project);
                    }
                    organization = organizationInDb;
                }
            });
            projectInDb.setOrganizations(organizations);

            Set<Budget> budgets = project.getBudgets();
            budgets.forEach(budget -> {
                Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
                if (budgetInDb == null) {
                    throw new NotFoundException();
                } else {
                    if (budgetInDb.getProjects().stream().noneMatch(prj -> projectInDb.getId().equals(prj.getId()))) {
                        budgetInDb.getProjects().add(project);
                    }
                    budget = budgetInDb;
                }
            });
            project.setBudgets(budgets);

            projectInDb.setTitle(project.getTitle());
            projectInDb.setShortDescription(project.getShortDescription());
            projectInDb.setLongDescription(project.getLongDescription());
            projectInDb.setLeader(project.getLeader());
            projectInDb.setPeopleRequired(project.getPeopleRequired());
            if (project.getDonationsRequired() > projectInDb.getDonationsRequired()) {
                projectInDb.setDonationsRequired(project.getDonationsRequired());
            }
            return projectRepository.save(projectInDb);
        }
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void save(@PathVariable("id") String id, @RequestBody Project project) {
        Project projectInDb = projectRepository.findById(Long.valueOf(id)).orElse(null);
        if (projectInDb == null) {
            throw new NotFoundException();
        } else {
            projectRepository.save(project);
        }
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") String id) {
        projectRepository.deleteById(Long.valueOf(id));
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/join", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Project join(@PathVariable("id") Long id, Principal principal) {
        Project projectInDb = projectRepository.findById(id).orElse(null);
        if (projectInDb == null) {
            throw new NotFoundException();
        } else {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
            final User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());
            User userInPeopleGivingTime = projectInDb.getPeopleGivingTime().stream().filter(userGivingTime -> userLoggedIn.getId().equals(userGivingTime.getId())).findFirst().orElse(null);
            if (userInPeopleGivingTime == null) {
                projectInDb.addPeopleGivingTime(userLoggedIn);
            } else {
                projectInDb.getPeopleGivingTime().remove(userInPeopleGivingTime);
            }
            return projectRepository.save(projectInDb);
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
        Set<Project> projects = projectRepository.findAllByStatusAndFundingDeadlineLessThan(ProjectStatus.A_IN_PROGRESS, new Date());
        LOGGER.info("[processProjectFundingDeadlines] " + projects.size() + " project(s) found");
        projects.forEach(project -> {
            Set<Donation> donations = donationRepository.findAllByProjectId(project.getId());
            float totalDonations = 0f;
            for (Donation donation : donations) {
                totalDonations += donation.getAmount();
            }
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Project : " + project.getTitle());
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Teammates : " + project.getPeopleGivingTime().size() + " / " + project.getPeopleRequired());
            LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Donations : " + totalDonations + " € / " + project.getDonationsRequired() + " €");
            if (totalDonations >= project.getDonationsRequired()
                    && project.getPeopleGivingTime().size() >= project.getPeopleRequired()) {
                project.setStatus(ProjectStatus.B_READY);
                LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Status => B_READY");
            } else {
                project.setStatus(ProjectStatus.C_AVORTED);
                LOGGER.info("[processProjectFundingDeadlines][" + project.getId() + "] Status => C_AVORTED");
                donationRepository.deleteByProjectId(project.getId());
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
        Set<Project> projects = projectRepository.findAllByStatus(ProjectStatus.A_IN_PROGRESS);
        LOGGER.info("[notifyProjectsAlmostFinished] " + projects.size() + " project(s) found");
        projects.forEach(project -> {
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
        Project project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            throw new NotFoundException();
        } else {
            long diffInMillies = Math.abs(project.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
            notifyProjectStatus(project, diff);
        }
    }

    public void notifyProjectStatus(Project project, long daysUntilDeadline) {

        int teamMatesMissing = project.getPeopleRequired() - project.getPeopleGivingTime().size();
        LOGGER.info("[notifyProjectStatus][" + project.getId() + "] Teammates missing : " + teamMatesMissing);

        Set<Donation> donations = donationRepository.findAllByProjectId(project.getId());
        float totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        float donationsMissing = project.getDonationsRequired() - totalDonations;
        LOGGER.info("[notifyProjectsAlmostFinished][" + project.getId() + "] Donations : " + donationsMissing + " €");

        if(teamMatesMissing > 0 || donationsMissing > 0) {

            User leader = project.getLeader();

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
                    .append(project.getTitle())
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

            project.getOrganizations().forEach(organization -> {
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
                                        .append(project.getId());
                            },() -> {
                                stringBuilderUser.append(defaultUser)
                                        .append(endMessage.toString())
                                        .append("\nRendez-vous à l'adresse suivante pour participer : \n")
                                        .append(WEB_URL)
                                        .append("/projects/")
                                        .append(project.getId());
                            });
                    LOGGER.info("[notifyProjectStatus][" + project.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + stringBuilderUser.toString());
                    String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                    slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                    slackClientService.postMessage(organization.getSlackTeam(), channelId, stringBuilderUser.toString());
                    LOGGER.info("[notifyProjectStatus][" + project.getId() + "] Slack Message Sent");
                }
            });

        }
    }
}
