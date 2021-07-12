package fr.lesprojetscagnottes.core.donation.controller;

import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import fr.lesprojetscagnottes.core.budget.repository.AccountRepository;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.campaign.CampaignEntity;
import fr.lesprojetscagnottes.core.campaign.CampaignRepository;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.donation.task.DonationProcessingTask;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.campaign.CampaignStatus;
import fr.lesprojetscagnottes.core.donation.queue.DonationOperationType;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/api")
@Tag(name = "Donations", description = "The Donations API")
@RestController
public class DonationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationController.class);

    @Autowired
    private DonationProcessingTask donationProcessingTask;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

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

    @Operation(summary = "Control donation amounts ", description = "Control donation amounts with accounts, campaigns and budgets", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Control started", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public void control(Principal principal) {
        LOGGER.info("Donation amounts control started");
        LOGGER.info("Control users");
        List<UserEntity> users = userRepository.findAll();
        users.forEach(user -> {
            LOGGER.info("User {} : {} {}", user.getId(), user.getFirstname(), user.getLastname());
            Set<BudgetEntity> budgets = budgetRepository.findAllByUser(user.getId());
            Set<AccountEntity> accounts = accountRepository.findAllByOwnerId(user.getId());
            if(budgets.size() != accounts.size()) {
                LOGGER.warn("Number of accounts {} and budgets {} for user {} don't match", accounts.size(), budgets.size(), user.getId());
            }
            budgets.forEach(budget -> {
                LOGGER.info("|- Budget {} : {}", budget.getId(), budget.getAmountPerMember());
                Optional<AccountEntity> accountOptional = accounts.stream().filter(account -> account.getBudget().getId().equals(budget.getId())).findFirst();
                if(accountOptional.isEmpty()) {
                    LOGGER.error("Not account found for budget {} and user {}", budget.getId(), user.getId());
                } else {
                    AccountEntity account = accountOptional.get();
                    LOGGER.info("|- Account {} : {} / {}", account.getId(), account.getAmount(), account.getInitialAmount());
                    if(account.getInitialAmount() != budget.getAmountPerMember()) {
                        LOGGER.error("Initial amount for account {} ({}) dont match with budget amount per member {} ({})", account.getId(), account.getInitialAmount(), budget.getId(), budget.getAmountPerMember());
                    }
                    Set<Donation> accountDonations = donationRepository.findAllByAccountId(account.getId());
                    final float[] totalDonationsAmount = {0f};
                    accountDonations.forEach(donation -> {
                        totalDonationsAmount[0] += donation.getAmount();
                    });
                    LOGGER.info("|- Total donations : {}", totalDonationsAmount[0]);
                    if(account.getAmount() != account.getInitialAmount() - totalDonationsAmount[0]) {
                        LOGGER.error("Total donations computed doest match with account amount ({} - {} != {})", account.getInitialAmount(), totalDonationsAmount[0], account.getAmount());
                    }
                }
            });
        });
        LOGGER.info("Control budgets");
        List<BudgetEntity> budgets = budgetRepository.findAll();
        budgets.forEach(budget -> {
            LOGGER.info("Budget {} : {}", budget.getId(), budget.getAmountPerMember());
            Set<AccountEntity> accounts = accountRepository.findAllByBudgetId(budget.getId());
            final float[] totalDonationsAmount = {0f};
            accounts.forEach(account -> {
                float accountTotalDonations = account.getInitialAmount() - account.getAmount();
                LOGGER.info("|- Account {} : {} - {} = {}", account.getId(), account.getInitialAmount(), account.getAmount(), accountTotalDonations);
                totalDonationsAmount[0] += accountTotalDonations;
            });
            LOGGER.info("|- Total donations : {}", totalDonationsAmount[0]);
            if(budget.getTotalDonations() != totalDonationsAmount[0]) {
                LOGGER.error("Account amounts doest match with budget total donations registered ({} != {})", totalDonationsAmount[0], budget.getTotalDonations());
            }
        });
        LOGGER.info("Control campaigns");
        List<CampaignEntity> campaigns = campaignRepository.findAll();
        campaigns.forEach(campaign -> {
            LOGGER.info("Campaign {} : {}", campaign.getId(), campaign.getTitle());
            Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
            final float[] totalDonationsAmount = {0f};
            donations.forEach(donation -> {
                LOGGER.info("|- Donation {} : {}", donation.getId(), donation.getAmount());
                totalDonationsAmount[0] += donation.getAmount();
            });
            LOGGER.info("|- Total donations : {}", totalDonationsAmount[0]);
            if(campaign.getTotalDonations() != totalDonationsAmount[0]) {
                LOGGER.error("Total donations computed doest match with campaign total registered ({} != {})", totalDonationsAmount[0], campaign.getTotalDonations());
            }
        });
        LOGGER.info("Donation amounts control finished");
    }

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
        if(donation == null || donation.getAccount() == null || donation.getCampaign() == null || donation.getContributor() == null || donation.getBudget() == null
        || donation.getCampaign().getId() == null || donation.getContributor().getId() == null || donation.getBudget().getId() == null) {
            LOGGER.error("Impossible to create donation : body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        AccountEntity account = accountRepository.findById(donation.getAccount().getId()).orElse(null);
        CampaignEntity campaign = campaignRepository.findById(donation.getCampaign().getId()).orElse(null);
        BudgetEntity budget = budgetRepository.findById(donation.getBudget().getId()).orElse(null);
        UserEntity contributor = userRepository.findById(donation.getContributor().getId()).orElse(null);

        // Verify that any of references are not null
        if(account == null || campaign == null || budget == null || contributor == null) {
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
        if(!userService.isMemberOfOrganization(userLoggedInId, budget.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to create donation : principal {} is not member of organization {}", userLoggedInId, budget.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Verify that status of campaign is in progress
        if(!campaign.getStatus().equals(CampaignStatus.IN_PROGRESS)) {
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
        Long budgetId = budget.getId();
        if(campaign.getBudgets().stream().noneMatch(projectBudget -> projectBudget.getId().equals(budgetId))) {
            LOGGER.error("Impossible to create donation : budgets is not associated with the campaign");
            throw new BadRequestException();
        }

        // Verify that amount is lower than account initial amount
        if(account.getInitialAmount() < donation.getAmount()) {
            LOGGER.error("Impossible to create donation : donation amount is greater than account initial amount");
            throw new BadRequestException();
        }

        // Otherwise save donation
        float amount = donation.getAmount();
        Donation donationToSave = new Donation();
        donationToSave.setAccount(account);
        donationToSave.setCampaign(campaign);
        donationToSave.setBudget(budget);
        donationToSave.setContributor(contributor);
        donationToSave.setAmount(amount);

        // Add donation to queue
        donationProcessingTask.insert(donationToSave, DonationOperationType.CREATION);
    }

    @Operation(summary = "Delete a donation by its ID", description = "Delete a donation by its ID", tags = { "Donations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donation deleted", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Donation not found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/donation/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(Principal principal, @PathVariable("id") long id) {

        // Fails if campaign ID is missing
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
        if(userLoggedInId != donation.getContributor().getId() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to delete donation : principal {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that campaign associated is in progress
        if(donation.getCampaign().getStatus() != CampaignStatus.IN_PROGRESS) {
            LOGGER.error("Impossible to delete donation : campaign {} is in progress", donation.getCampaign());
            throw new ForbiddenException();
        }

        // Delete donation
        donationProcessingTask.insert(donation, DonationOperationType.DELETION);
    }


}
