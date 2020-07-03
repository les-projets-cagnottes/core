package fr.lesprojetscagnottes.core.controller;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.model.BudgetModel;
import fr.lesprojetscagnottes.core.model.CampaignModel;
import fr.lesprojetscagnottes.core.model.DonationModel;
import fr.lesprojetscagnottes.core.model.OrganizationModel;
import fr.lesprojetscagnottes.core.pagination.DataPage;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.scheduler.CampaignScheduler;
import fr.lesprojetscagnottes.core.service.SlackClientService;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api")
@Tag(name = "Campaigns", description = "The Campaigns API")
public class CampaignController {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignController.class);

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
            LOGGER.error("Impossible to get campaign by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in campaign organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<Organization> campaignOrganizations = organizationRepository.findAllByCampaigns_Id(id);
        Set<Organization> principalOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.isNotAdmin(userLoggedInId) && campaignOrganizations.stream().noneMatch(principalOrganizations::contains)) {
            LOGGER.error("Impossible to get campaign by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        Campaign entity = campaignRepository.findById(id).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to get campaign by ID : campaign not found");
            throw new NotFoundException();
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
    public Set<CampaignModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<Organization> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<CampaignModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            Campaign campaign = campaignRepository.findById(id).orElse(null);
            if(campaign == null) {
                LOGGER.error("Impossible to get campaign {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<Organization> campaignOrganizations = organizationRepository.findAllByCampaigns_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, campaignOrganizations) && userLoggedIn_isNotAdmin) {
                LOGGER.error("Impossible to get campaign {} : principal {} is not in its organizations", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(CampaignModel.fromEntity(campaign));
        }

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
            LOGGER.error("Impossible to get budgets by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get budgets by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Campaign campaign = campaignRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            LOGGER.error("Impossible to get budgets by campaign ID : campaign {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<Budget> entities = budgetRepository.findAllByCampaigns_Id(id);
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
            LOGGER.error("Impossible to get donations by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, campaignId).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            LOGGER.error("Impossible to get donations by campaign ID : campaign {} not found", campaignId);
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
            LOGGER.error("Impossible to get organizations by campaign ID : Campaign ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the campaign
        long userLoggedInId = userService.get(principal).getId();
        if(campaignRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get organizations by campaign ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Campaign campaign = campaignRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(campaign == null) {
            LOGGER.error("Impossible to get organizations by campaign ID : campaign {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<Organization> entities = organizationRepository.findAllByCampaigns_Id(id);
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
        if(campaign == null || campaign.getLeader() == null || campaign.getLeader().getId() < 0 ||
            campaign.getOrganizationsRef() == null || campaign.getOrganizationsRef().isEmpty() ||
            campaign.getBudgetsRef() == null || campaign.getBudgetsRef().isEmpty()) {
            if(campaign != null ) {
                LOGGER.error("Impossible to create campaign \"{}\" : some references are missing", campaign.getTitle());
            } else {
                LOGGER.error("Impossible to create a null campaign");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        User leader = userRepository.findById(campaign.getLeader().getId()).orElse(null);
        Set<Organization> organizations = organizationRepository.findAllByIdIn(campaign.getOrganizationsRef());
        Set<Budget> budgets = budgetRepository.findAllByIdIn(campaign.getBudgetsRef());

        // Fails if any of references are null
        if(leader == null || organizations.isEmpty() || budgets.isEmpty() ||
            organizations.size() != campaign.getOrganizationsRef().size() ||
            budgets.size() != campaign.getBudgetsRef().size()) {
            LOGGER.error("Impossible to create campaign \"{}\" : one or more reference(s) doesn't exist", campaign.getTitle());
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        User userLoggedIn = userService.get(principal);
        Set<Organization> organizationsMatch = organizationRepository.findAllByIdInAndMembers_Id(campaign.getOrganizationsRef(), userLoggedIn.getId());
        if(organizationsMatch.isEmpty()) {
            LOGGER.error("Impossible to create campaign \"{}\" : principal {} is not member of all organizations", campaign.getTitle(), userLoggedIn.getId());
            throw new ForbiddenException();
        }

        // Verify that budgets are usable
        Set<Budget> budgetsUsable = budgetRepository.findAllUsableBudgetsInOrganizations(new Date(), campaign.getOrganizationsRef());
        if(!budgetsUsable.containsAll(budgets)) {
            LOGGER.error("Impossible to create campaign \"{}\" : budgets are not all usable", campaign.getTitle());
            throw new ForbiddenException();
        }

        // Save campaign
        Campaign campaignToSave = new Campaign();
        campaignToSave.setTitle(campaign.getTitle());
        campaignToSave.setStatus(CampaignStatus.A_IN_PROGRESS);
        campaignToSave.setShortDescription(campaign.getShortDescription());
        campaignToSave.setLongDescription(campaign.getLongDescription());
        campaignToSave.setDonationsRequired(campaign.getDonationsRequired());
        campaignToSave.setPeopleRequired(campaign.getPeopleRequired());
        campaignToSave.setFundingDeadline(campaign.getFundingDeadline());
        campaignToSave.setTotalDonations(0f);
        campaignToSave.setLeader(leader);
        campaignToSave.getPeopleGivingTime().add(leader);
        final Campaign campaignFinal = campaignRepository.save(campaignToSave);

        // Associate the campaign with organizations
        organizations.forEach(organization -> {
            organization.getCampaigns().add(campaignFinal);
            campaignFinal.getOrganizations().add(organization);
        });
        organizationRepository.saveAll(organizations);

        // Associate the campaign with budgets
        budgets.forEach(budget -> {
            budget.getCampaigns().add(campaignFinal);
            campaignFinal.getBudgets().add(budget);
        });
        budgetRepository.saveAll(budgets);

        Map<String, Object> model = new HashMap<>();
        model.put("URL", WEB_URL);
        model.put("campaign", campaignFinal);

        organizations.forEach(organization -> {
            if(organization.getSlackTeam() != null) {
                organization.getMembers().stream()
                        .filter(member -> member.getId().equals(leader.getId()))
                        .findAny()
                        .ifPresentOrElse(member -> {
                            organization.getSlackTeam().getSlackUsers().stream()
                                    .filter(slackUser -> slackUser.getUser().getId().equals(leader.getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                            slackUser -> model.put("leader", "<@" + slackUser.getSlackId() + ">"),
                                            () -> model.put("leader", leader.getFullname()));
                        },() -> model.put("leader", leader.getFullname()));

                Context context = new Context();
                context.setVariables(model);
                String slackMessage = templateEngine.process("slack/fr/campaign-created", context);

                LOGGER.info("[create][" + campaign.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + slackMessage);
                String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                slackClientService.postMessage(organization.getSlackTeam(), channelId, slackMessage);
                LOGGER.info("[create][" + campaign.getId() + "] Slack Message Sent");
            }
        });

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
        if(campaignModel == null || campaignModel.getId() <= 0 || campaignModel.getLeader() == null || campaignModel.getLeader().getId() < 0) {
            if(campaignModel != null ) {
                LOGGER.error("Impossible to update campaign {} : some references are missing", campaignModel.getId());
            } else {
                LOGGER.error("Impossible to update a null campaign");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        User leader = userRepository.findById(campaignModel.getLeader().getId()).orElse(null);
        Campaign campaign = campaignRepository.findById(campaignModel.getId()).orElse(null);

        // Fails if any of references are null
        if(campaign == null || leader == null) {
            LOGGER.error("Impossible to update campaign {} : one or more reference(s) doesn't exist", campaignModel.getId());
            throw new NotFoundException();
        }

        // Verify that principal has enough privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!leader.getId().equals(userLoggedInId) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to update campaign {} : principal has not enough privileges", campaignModel.getId());
        }

        // Save campaign
        campaign.setTitle(campaign.getTitle());
        campaign.setShortDescription(campaign.getShortDescription());
        campaign.setLongDescription(campaign.getLongDescription());
        campaign.setLeader(campaign.getLeader());
        campaign.setPeopleRequired(campaign.getPeopleRequired());
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
            LOGGER.error("Impossible to join campaign {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Campaign campaign = campaignRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(campaign == null) {
            LOGGER.error("Impossible to join campaign {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        User userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        campaign.setOrganizations(organizationRepository.findAllByCampaigns_Id(id));
        Set<Long> organizationsRef = new LinkedHashSet<>();
        campaign.getOrganizations().forEach(organization -> organizationsRef.add(organization.getId()));
        if(organizationRepository.findAllByIdInAndMembers_Id(organizationsRef, userLoggedInId).isEmpty()) {
            LOGGER.error("Impossible to join campaign {} : principal {} is not member of all organizations", id, userLoggedInId);
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
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if(campaign == null) {
            throw new NotFoundException();
        } else {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
            campaignScheduler.notifyCampaignStatus(campaign, diff);
        }
    }

}
