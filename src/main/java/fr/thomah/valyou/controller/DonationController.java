package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.entity.model.DonationModel;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.pagination.DataPage;
import fr.thomah.valyou.service.UserService;
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

import java.security.Principal;
import java.util.*;

@RequestMapping("/api")
@Tag(name = "Donations", description = "The Donations API")
@RestController
public class DonationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationController.class);

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

    @Operation(summary = "Submit a donation", description = "Submit a new donation", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation was successfully made", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "At least one reference wasn't found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/donation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(Principal principal, @RequestBody DonationModel donation) {

        // Verify that body is complete
        if(donation == null || donation.getProject() == null || donation.getContributor() == null || donation.getBudget() == null
        || donation.getProject().getId() == null || donation.getContributor().getId() == null || donation.getBudget().getId() == null) {
            LOGGER.error("Impossible to create donation : body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Project campaign = projectRepository.findById(donation.getProject().getId()).orElse(null);
        Budget budget = budgetRepository.findById(donation.getBudget().getId()).orElse(null);
        User contributor = userRepository.findById(donation.getContributor().getId()).orElse(null);

        // Verify that any of references are not null
        if(campaign == null || budget == null || contributor == null) {
            LOGGER.error("Impossible to create donation : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is the contributor
        User userLoggedIn = userService.get(principal);
        if(!userLoggedIn.getId().equals(contributor.getId())) {
            LOGGER.error("Impossible to create donation : principal {} is not the contributor", userLoggedIn.getId());
            throw new ForbiddenException();
        }

        // Verify that principal is member of organization
        Optional<Organization> organization = organizationRepository.findByIdAndMembers_Id(budget.getOrganization().getId(), userLoggedIn.getId());
        if(organization.isEmpty()) {
            LOGGER.error("Impossible to create donation : principal {} is not member of organization {}", userLoggedIn.getId(), budget.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Verify that status of campaign is in progress
        if(!campaign.getStatus().equals(ProjectStatus.A_IN_PROGRESS)) {
            LOGGER.error("Impossible to create donation : status of campaign is not in progress");
            throw new BadRequestException();
        }

        // Verify that funding deadline of campaign has not been reached
        Date now = new Date();
        if(now.compareTo(campaign.getFundingDeadline()) > 0) {
            LOGGER.error("Impossible to create donation : funding deadline of campaign has been reached");
            throw new BadRequestException();
        }

        // Verify that donation budgets is associated with the campaign
        if(campaign.getBudgets().stream().noneMatch(projectBudget -> projectBudget.getId().equals(budget.getId()))) {
            LOGGER.error("Impossible to create donation : budgets is not associated with the campaign");
            throw new BadRequestException();
        }

        // Verify that contributor has enough amount on the budget
        Set<Donation> contributorDonations = donationRepository.findAllByContributorIdAndBudgetId(contributor.getId(), budget.getId());
        float totalAmount = 0;
        for(Donation contributorDonation : contributorDonations) {
            totalAmount+= contributorDonation.getAmount();
        }
        if(totalAmount + donation.getAmount() > budget.getAmountPerMember()) {
            LOGGER.error("Impossible to create donation : contributor has not enough amount budget");
            throw new BadRequestException();
        }

        // Otherwise save donation
        Donation donationToSave = new Donation();
        donationToSave.setProject(campaign);
        donationToSave.setBudget(budget);
        donationToSave.setContributor(contributor);
        donationToSave.setAmount(donation.getAmount());
        donationRepository.save(donationToSave);
    }

    @Operation(summary = "Get donations imputed on a budget", description = "Get donations imputed on a budget", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationModel.class)))),
            @ApiResponse(responseCode = "400", description = "Budget ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId"})
    public Set<DonationModel> getByBudgetId(Principal principal, @RequestParam("budgetId") long budgetId) {

        // Fails if budget ID is missing
        if(budgetId <= 0) {
            LOGGER.error("Impossible to get donations by budget ID : Budget ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Budget budget = budgetRepository.findById(budgetId).orElse(null);

        // Verify that any of references are not null
        if(budget == null) {
            LOGGER.error("Impossible to get donations by budget ID : budget not found");
            throw new NotFoundException();
        }

        // Verify that principal is member of organization
        User userLoggedIn = userService.get(principal);
        Optional<Organization> organization = organizationRepository.findByIdAndMembers_Id(budget.getOrganization().getId(), userLoggedIn.getId());
        if(organization.isEmpty()) {
            LOGGER.error("Impossible to get donations by budget ID : principal {} is not member of organization {}", userLoggedIn.getId(), budget.getOrganization().getId());
            throw new ForbiddenException();
        }

        Set<Donation> entities = donationRepository.findAllByBudgetId(budgetId);
        Set<DonationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(DonationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get donations made on a project", description = "Get donations made on a project", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationModel.class)))),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"projectId"})
    public DataPage<DonationModel> getByProjectId(Principal principal, @RequestParam("projectId") long projectId, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        // Fails if budget ID is missing
        if(projectId <= 0) {
            LOGGER.error("Impossible to get donations by project ID : Project ID is incorrect");
            throw new BadRequestException();
        }


//        select p.* from projects p " +
//        "inner join project_organizations on p.id = project_organizations.project_id " +
//                "inner join organizations o on project_organizations.organization_id = o.id " +
//                "inner join organizations_users on organizations_users.organization_id = o.id " +
//                "inner join users u on u.id = organizations_users.user_id " +
//                "where u.id = :user_id and p.id = :project_id")

        List<Organization> organizationList = organizationRepository.findAll();
        organizationList.forEach(organization -> {
            LOGGER.debug(organization.getName());
            organization.getProjects().forEach(project -> {
                LOGGER.debug(project.getTitle());
            });
        });

        // Verify that principal is in one organization of the project
        long userLoggedInId = userService.get(principal).getId();
        if(projectRepository.findAllProjectsByUserInOrganizations(userLoggedInId, projectId).isEmpty()) {
            LOGGER.error("Impossible to get donations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(projectId).orElse(null);

        // Verify that any of references are not null
        if(project == null) {
            LOGGER.error("Impossible to get donations by project ID : project not found");
            throw new NotFoundException();
        }

        // Get and transform donations
        Page<Donation> entities = donationRepository.findByProject_idOrderByIdAsc(projectId, PageRequest.of(offset, limit, Sort.by("id")));
        DataPage<DonationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(DonationModel.fromEntity(entity)));
        return models;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId"})
    public Set<Donation> getByContributorId(@RequestParam("contributorId") long contributorId) {
        return donationRepository.findAllByContributorIdOrderByBudgetIdAsc(contributorId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId", "budgetId"})
    public Set<Donation> getByContributorIdAndBudgetId(@RequestParam("contributorId") long contributorId, @RequestParam("budgetId") long budgetId) {
        return donationRepository.findAllByContributorIdAndBudgetId(contributorId, budgetId);
    }

    @RequestMapping(value = "/donation/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id) {
        Donation donation = donationRepository.findById(id).orElse(null);
        if(donation == null) {
            throw new NotFoundException();
        } else if(donation.getProject().getStatus() == ProjectStatus.A_IN_PROGRESS) {
            donationRepository.deleteById(id);
        }
    }


}
