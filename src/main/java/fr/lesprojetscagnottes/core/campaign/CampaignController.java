package fr.lesprojetscagnottes.core.campaign;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationModel;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.ProjectEntity;
import fr.lesprojetscagnottes.core.project.ProjectModel;
import fr.lesprojetscagnottes.core.project.ProjectRepository;
import fr.lesprojetscagnottes.core.project.ProjectStatus;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Campaigns", description = "The Campaigns API")
public class CampaignController {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    @Autowired
    private Gson gson;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private CampaignScheduler campaignScheduler;

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
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find a campaign by its ID", description = "Find a campaign by its ID", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the campaign", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignModel findById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            log.error("Impossible to get campaign by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        CampaignEntity entity = campaignRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to get campaign by ID : campaign not found");
            throw new NotFoundException();
        }

        // Verify that principal is in campaign organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> campaignOrganizations = organizationRepository.findAllByProjects_Id(entity.getProject().getId());
        Set<OrganizationEntity> principalOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.isNotAdmin(userLoggedInId) && campaignOrganizations.stream().noneMatch(principalOrganizations::contains)) {
            log.error("Impossible to get campaign by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Transform and return organization
        return CampaignModel.fromEntity(entity);
    }

    @Operation(summary = "Get list of campaigns by a list of IDs", description = "Find a list of campaigns by a list of IDs", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the campaigns", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public List<CampaignModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<OrganizationEntity> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        List<CampaignModel> models = new ArrayList<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            CampaignEntity campaign = campaignRepository.findById(id).orElse(null);
            if(campaign == null) {
                log.error("Impossible to get campaign {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> campaignOrganizations = organizationRepository.findAllByProjects_Id(campaign.getProject().getId());
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, campaignOrganizations) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get campaign {} : principal {} is not in organizations of project's campaign", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(CampaignModel.fromEntity(campaign));
        }

        Comparator<CampaignModel> compareByFundingDeadline = Comparator.comparing(CampaignModel::getFundingDeadline);
        models.sort(compareByFundingDeadline.reversed());

        return models;
    }

    @Operation(summary = "Get campaign budgets", description = "Get campaign budgets", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding budgets", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class)))),
            @ApiResponse(responseCode = "400", description = "Campaign ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}/budgets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<BudgetModel> getBudgets(Principal principal, @PathVariable("id") Long id) {

        // Fails if campaign ID is missing
        if(id <= 0) {
            log.error("Impossible to get budgets by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get budgets by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        CampaignEntity campaign = campaignRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            log.error("Impossible to get budgets by campaign ID : campaign {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<BudgetEntity> entities = budgetRepository.findAllByCampaigns_Id(id);
        Set<BudgetModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(BudgetModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get paginated donations made on a campaign", description = "Get paginated donations made on a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding paginated donations", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Campaign ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<DonationModel> getDonations(Principal principal, @PathVariable("id") long campaignId, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        // Fails if campaign ID is missing
        if(campaignId <= 0) {
            log.error("Impossible to get donations by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, campaignId).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get donations by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        CampaignEntity campaign = campaignRepository.findById(campaignId).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            log.error("Impossible to get donations by campaign ID : campaign {} not found", campaignId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Page<Donation> entities = donationRepository.findByCampaign_idOrderByIdAsc(campaignId, PageRequest.of(offset, limit, Sort.by("id")));
        DataPage<DonationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(DonationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get campaign organizations", description = "Get campaign organizations", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding organizations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationModel.class)))),
            @ApiResponse(responseCode = "400", description = "Campaign ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<OrganizationModel> getOrganizations(Principal principal, @PathVariable("id") Long id) {

        // Fails if campaign ID is missing
        if(id <= 0) {
            log.error("Impossible to get organizations by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get organizations by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        CampaignEntity campaign = campaignRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            log.error("Impossible to get organizations by campaign ID : campaign {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<OrganizationEntity> entities = organizationRepository.findAllByCampaigns_Id(id);
        Set<OrganizationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Create a campaign", description = "Create a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campaign created", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignModel create(Principal principal, @RequestBody CampaignModel campaign) {

        // Fails if any of references are null
        if(campaign == null || campaign.getProject() == null || campaign.getProject().getId() <= 0 || campaign.getBudgetsRef() == null || campaign.getBudgetsRef().isEmpty()) {
            if(campaign != null ) {
                log.error("Impossible to create campaign : some references are missing");
            } else {
                log.error("Impossible to create a null campaign");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(campaign.getProject().getId()).orElse(null);
        Set<BudgetEntity> budgets = budgetRepository.findAllByIdIn(campaign.getBudgetsRef());

        // Fails if any of references are null
        if(project == null || budgets.isEmpty() || budgets.size() != campaign.getBudgetsRef().size()) {
            log.error("Impossible to create campaign \"{}\" : one or more reference(s) doesn't exist", campaign.getTitle());
            throw new NotFoundException();
        }

        // Verify that principal is project leader
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(project.getLeader().getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to create campaign \"{}\" : principal {} is not project leader", campaign.getTitle(), userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that budgets are usable
        Set<BudgetEntity> budgetsUsable = budgetRepository.findAllUsableBudgetsInOrganizations(new Date(), ProjectModel.fromEntity(project).getOrganizationsRef());
        if(!budgetsUsable.containsAll(budgets)) {
            log.error("Impossible to create campaign : budgets are not all usable");
            throw new ForbiddenException();
        }

        // Save campaign
        CampaignEntity campaignToSave = new CampaignEntity();
        campaignToSave.setStatus(CampaignStatus.IN_PROGRESS);
        campaignToSave.setDonationsRequired(campaign.getDonationsRequired());
        campaignToSave.setFundingDeadline(campaign.getFundingDeadline());
        campaignToSave.setTotalDonations(0f);
        campaignToSave.setLeader(null);
        campaignToSave.setProject(project);
        final CampaignEntity campaignFinal = campaignRepository.save(campaignToSave);

        // Associate the campaign with budgets
        budgets.forEach(budget -> {
            budget.getCampaigns().add(campaignFinal);
            campaignFinal.getBudgets().add(budget);
        });
        budgetRepository.saveAll(budgets);

        Map<String, Object> model = new HashMap<>();
        model.put("URL", WEB_URL);
        model.put("campaign", campaignFinal);

        // Send a notification if project is in progress state
        if(project.getStatus().equals(ProjectStatus.IN_PROGRESS)) {
            Set<OrganizationEntity> organizations = project.getOrganizations();
            UserEntity leader = project.getLeader();
            organizations.forEach(organization -> {
                if(organization.getSlackTeam() != null) {

                    SlackTeamEntity slackTeam = organization.getSlackTeam();

                    organization.getMembers().stream()
                            .filter(member -> member.getId().equals(leader.getId()))
                            .findAny()
                            .ifPresentOrElse(member -> slackTeam.getSlackUsers().stream()
                                    .filter(slackUser -> slackUser.getUser().getId().equals(leader.getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                            slackUser -> model.put("leader", "<@" + slackUser.getSlackId() + ">"),
                                            () -> model.put("leader", leader.getFullname())),() -> model.put("leader", leader.getFullname()));

                    Context context = new Context();
                    context.setVariables(model);
                    String slackMessage = templateEngine.process("slack/fr/campaign-created", context);

                    log.info("[create][" + campaign.getId() + "] Send Slack Message to " + slackTeam.getTeamId() + " / " + slackTeam.getPublicationChannelId() + " :\n" + slackMessage);

                    slackClientService.inviteBotInConversation(slackTeam);
                    slackClientService.postMessage(slackTeam, slackTeam.getPublicationChannelId(), slackMessage);
                    log.info("[create][" + campaign.getId() + "] Slack Message Sent");
                }
            });
        }
        campaign.setId(campaignFinal.getId());
        return campaign;
    }

    @Operation(summary = "Update a campaign", description = "Update a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign updated", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public CampaignModel update(Principal principal, @RequestBody CampaignModel campaignModel) {

        // Fails if any of references are null
        if(campaignModel == null || campaignModel.getProject() == null || campaignModel.getProject().getId() <= 0 || campaignModel.getBudgetsRef() == null || campaignModel.getBudgetsRef().isEmpty()) {
            if(campaignModel != null ) {
                log.error("Impossible to update campaign {} : some references are missing", campaignModel.getId());
            } else {
                log.error("Impossible to update a null campaign");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(campaignModel.getProject().getId()).orElse(null);
        Set<BudgetEntity> budgets = budgetRepository.findAllByIdIn(campaignModel.getBudgetsRef());
        CampaignEntity campaign = campaignRepository.findById(campaignModel.getId()).orElse(null);

        // Fails if any of references are null
        if(project == null || budgets.isEmpty() || campaign == null || campaign.getBudgetsRef() == null || budgets.size() != campaign.getBudgetsRef().size()) {
            log.error("Impossible to update campaign \"{}\" : one or more reference(s) doesn't exist", campaignModel.getId());
            throw new NotFoundException();
        }

        // Verify that principal is project leader
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(project.getLeader().getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to update campaign \"{}\" : principal {} is not project leader", campaignModel.getId(), userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that budgets are usable
        Set<BudgetEntity> budgetsUsable = budgetRepository.findAllUsableBudgetsInOrganizations(new Date(), ProjectModel.fromEntity(project).getOrganizationsRef());
        if(!budgetsUsable.containsAll(budgets)) {
            log.error("Impossible to update campaign : budgets are not all usable");
            throw new ForbiddenException();
        }

        // Save campaign
        if (campaignModel.getDonationsRequired() > campaign.getDonationsRequired()) {
            campaign.setDonationsRequired(campaignModel.getDonationsRequired());
        }
        return CampaignModel.fromEntity(campaignRepository.save(campaign));
    }

    @Operation(summary = "Join a campaign", description = "Make principal join the campaign as a member", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign joined", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}/join", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void join(Principal principal, @PathVariable("id") Long id) {

        // Fails if any of references are null
        if(id < 0) {
            log.error("Impossible to join campaign {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        CampaignEntity campaign = campaignRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(campaign == null) {
            log.error("Impossible to join campaign {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        campaign.setOrganizations(organizationRepository.findAllByCampaigns_Id(id));
        Set<Long> organizationsRef = new LinkedHashSet<>();
        campaign.getOrganizations().forEach(organization -> organizationsRef.add(organization.getId()));
        if(organizationRepository.findAllByIdInAndMembers_Id(organizationsRef, userLoggedInId).isEmpty()) {
            log.error("Impossible to join campaign {} : principal {} is not member of all organizations", id, userLoggedInId);
            throw new ForbiddenException();
        }

        // Add or remove member
        campaign.setPeopleGivingTime(userRepository.findAllByCampaigns_Id(id));
        campaign.getPeopleGivingTime()
                .stream()
                .filter(member -> userLoggedInId.equals(member.getId()))
                .findAny()
                .ifPresentOrElse(
                        user -> campaign.getPeopleGivingTime().remove(user),
                        () -> campaign.getPeopleGivingTime().add(userLoggedIn));
        campaignRepository.save(campaign);
    }

    @Operation(summary = "Execute campaign validation", description = "Execute campaign validation without waiting for the CRON", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation executed with success", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void validate() {
        campaignScheduler.processCampaignFundingDeadlines();
    }

    @Operation(summary = "Execute campaign notification", description = "Execute campaign notification without waiting for the CRON", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification executed with success", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void notifyCampaignsAlmostFinished(Principal principal) {
        campaignScheduler.notifyCampaignsAlmostFinished();
    }

    @RequestMapping(value = "/campaign/{id}/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void notifyCampaignStatus(@PathVariable("id") long id) {
        CampaignEntity campaign = campaignRepository.findById(id).orElse(null);
        if(campaign == null) {
            throw new NotFoundException();
        } else {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
            campaignScheduler.notifyCampaignStatus(campaign, diff);
        }
    }

}
