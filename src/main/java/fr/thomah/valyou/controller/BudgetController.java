package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.entity.model.BudgetModel;
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
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Budgets", description = "The Budgets API")
public class BudgetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetController.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find all usable budgets for the current user", description = "A usable budget has an unreached end date and is distributed", tags = { "Budgets" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all usable budgets for the current user", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class))))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget/usable", method = RequestMethod.GET)
    public Set<BudgetModel> getUsableBudgets(Principal principal) {

        // Get user organizations
        User user = userService.get(principal);
        Set<Organization> organizations = organizationRepository.findByMembers_Id(user.getId());

        // Put all organization IDs in a single list
        List<Long> organizationIds = new ArrayList<>();
        organizations.forEach(organization -> organizationIds.add(organization.getId()));

        // Retrieve all corresponding entities
        Set<Budget> entities = budgetRepository.findAllUsableBudgetsInOrganizations(new Date(), organizationIds);

        // Convert all entities to models
        Set<BudgetModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(BudgetModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Find all budgets for an organization", description = "Find all budgets for an organization", tags = { "Budgets" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all budgets for an organization", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class))))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"organizationId"})
    public Set<BudgetModel> getByOrganizationId(Principal principal, @RequestParam("organizationId") Long organizationId) {

        // Verify that principal is member of organization
        User user = userService.get(principal);
        Optional<Organization> organization = organizationRepository.findByIdAndMembers_Id(organizationId, user.getId());
        if(organization.isEmpty()) {
            throw new ForbiddenException();
        }

        // Get budget entities
        Set<Budget> entities = budgetRepository.findAllByOrganizationId(organizationId);

        // Convert all entities to models
        Set<BudgetModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(BudgetModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Create a budget", description = "Create a budget", tags = { "Budgets" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class)))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal or sponsor has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(Principal principal, @RequestBody Budget budget) {

        // Fails if any of references are null
        if(budget == null || budget.getOrganization() == null || budget.getSponsor() == null || budget.getRules() == null
                || budget.getOrganization().getId() == null || budget.getSponsor().getId() == null || budget.getRules().getId() == null) {
            if(budget != null ) {
                LOGGER.error("Impossible to create budget {} : some references are missing", budget.getName());
            } else {
                LOGGER.error("Impossible to create a null budget");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Organization organization = organizationRepository.findById(budget.getOrganization().getId()).orElse(null);
        User sponsor = userRepository.findById(budget.getSponsor().getId()).orElse(null);
        fr.thomah.valyou.entity.Content rules = contentRepository.findById(budget.getRules().getId()).orElse(null);

        // Fails if any of references are null
        if(organization == null || sponsor == null || rules == null) {
            LOGGER.error("Impossible to create budget {} : one or more reference(s) doesn't exist", budget.getName());
            throw new NotFoundException();
        }

        // Verify that principal is member of organization
        User userLoggedIn = userService.get(principal);
        Optional<Organization> sponsorOrganization = organizationRepository.findByIdAndMembers_Id(organization.getId(), userLoggedIn.getId());
        if(sponsorOrganization.isEmpty()) {
            LOGGER.error("Impossible to create budget {} : principal {} is not member of organization {}", budget.getName(), userLoggedIn.getId(), organization.getId());
            throw new ForbiddenException();
        }

        // Test that user logged in has correct rights
        if(organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organization.getId(), userLoggedIn.getId(), OrganizationAuthorityName.ROLE_SPONSOR) == null &&
                authorityRepository.findByNameAndUsers_Id(AuthorityName.ROLE_ADMIN, userLoggedIn.getId()) == null) {
            LOGGER.error("Impossible to create budget {} : principal {} has not enough privileges", budget.getName(), userLoggedIn.getId());
            throw new ForbiddenException();
        }

        // Test that sponsor has correct rights
        if(organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organization.getId(), sponsor.getId(), OrganizationAuthorityName.ROLE_SPONSOR) == null) {
            LOGGER.error("Impossible to create budget {} : sponsor {} has not enough privileges", budget.getName(), sponsor.getId());
            throw new ForbiddenException();
        }

        budgetRepository.save(budget);
    }

    @Operation(summary = "Save multiple budgets", description = "Save a collection of budgets", tags = { "Budgets" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets saved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class)))),
            @ApiResponse(responseCode = "400", description = "Body is missing", content = @Content(schema = @Schema())),
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(Principal principal, @RequestBody List<Budget> budgets) {

        // Fails if body is null
        if(budgets == null) {
            LOGGER.error("Impossible to save null budgets");
            throw new BadRequestException();
        }

        User userLoggedIn = userService.get(principal);
        boolean userLoggedIn_isAdmin = authorityRepository.findByNameAndUsers_Id(AuthorityName.ROLE_ADMIN, userLoggedIn.getId()) == null;

        for(Budget budget : budgets) {

            // Fails if any of references are null
            if(budget.getOrganization() == null || budget.getSponsor() == null || budget.getRules() == null
                    || budget.getOrganization().getId() == null || budget.getSponsor().getId() == null || budget.getRules().getId() == null) {
                LOGGER.error("Impossible to save budget {} : some references are missing", budget.getId());
                continue;
            }

            // Retrieve full referenced objects
            Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
            if(budgetInDb == null) {
                LOGGER.error("Impossible to save budget {} : ID not found", budget.getId());
                continue;
            }

            // Verify that principal is member of organization
            Optional<Organization> sponsorOrganization = organizationRepository.findByIdAndMembers_Id(budget.getOrganization().getId(), userLoggedIn.getId());
            if(sponsorOrganization.isEmpty()) {
                LOGGER.error("Impossible to create budget {} : principal {} is not member of organization {}", budget.getName(), userLoggedIn.getId(), budget.getOrganization().getId());
                continue;
            }

            // Test that user logged in has correct rights
            if(organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(budget.getOrganization().getId(), userLoggedIn.getId(), OrganizationAuthorityName.ROLE_SPONSOR) == null &&
                    userLoggedIn_isAdmin) {
                LOGGER.error("Impossible to create budget {} : principal {} has not enough privileges", budget.getName(), userLoggedIn.getId());
                continue;
            }

            // Test that sponsor has correct rights
            if(organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(budget.getOrganization().getId(), budget.getSponsor().getId(), OrganizationAuthorityName.ROLE_SPONSOR) == null) {
                LOGGER.error("Impossible to create budget {} : sponsor {} has not enough privileges", budget.getName(), budget.getSponsor().getId());
                continue;
            }

            // Update budget in DB
            budgetInDb.setName(budget.getName());
            if(!budget.getIsDistributed()) {
                budgetInDb.setStartDate(budget.getStartDate());
                budgetInDb.setEndDate(budget.getEndDate());
                budgetInDb.setAmountPerMember(budget.getAmountPerMember());
                budgetInDb.setSponsor(budget.getSponsor());
                budgetInDb.setRules(budget.getRules());
            }
            budgetRepository.save(budgetInDb);
        }
    }

    @RequestMapping(value = "/budget/{id}/distribute", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void distribute(@PathVariable("id") Long id) {
        Budget budget = budgetRepository.findById(id).orElse(null);
        if(budget == null) {
            throw new NotFoundException();
        } else {
            budget.setIsDistributed(!budget.getIsDistributed());
            budgetRepository.save(budget);
        }
    }

    @RequestMapping(value = "/budget/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id) {
        budgetRepository.deleteById(id);
    }

}
