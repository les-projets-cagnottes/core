package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.entity.model.DonationModel;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/api")
@Tag(name = "Donations", description = "The Donations API")
@RestController
public class DonationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationController.class);

    @Autowired
    private AuthorityRepository authorityRepository;

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
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != contributor.getId()) {
            LOGGER.error("Impossible to create donation : principal {} is not the contributor", userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that principal is member of organization
        Optional<Organization> organization = organizationRepository.findByIdAndMembers_Id(budget.getOrganization().getId(), userLoggedInId);
        if(organization.isEmpty() && !userService.isAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to create donation : principal {} is not member of organization {}", userLoggedInId, budget.getOrganization().getId());
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

    @Operation(summary = "Get donations made by a user imputed on a budget", description = "Get donations made by a user imputed on a budget", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationModel.class)))),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/donation/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(Principal principal, @PathVariable("id") long id) {

        // Fails if project ID is missing
        if(id <= 0) {
            LOGGER.error("Impossible to delete donation : ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Donation donation = donationRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(donation == null) {
            LOGGER.error("Impossible to delete donation : donation {} not found", id);
            throw new NotFoundException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != donation.getContributor().getId() && !userService.isAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to delete donation : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that campaign associated is in progress
        if(donation.getProject().getStatus() != ProjectStatus.A_IN_PROGRESS) {
            throw new ForbiddenException();
        }

        // Delete donation
        donationRepository.deleteById(id);
    }


}
