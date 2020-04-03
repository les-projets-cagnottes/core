package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.Budget;
import fr.thomah.valyou.entity.Organization;
import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.entity.model.BudgetModel;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import fr.thomah.valyou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

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
        organizations.forEach(organization -> {
            organizationIds.add(organization.getId());
        });

        // Retrieve all corresponding entities
        Set<Budget> entities = budgetRepository.findAllUsableBudgetsInOrganizations(new Date(), organizationIds);

        // Convert all entities to models
        Set<BudgetModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> {
            models.add(BudgetModel.fromEntity(entity));
        });

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
        entities.forEach(entity -> {
            models.add(BudgetModel.fromEntity(entity));
        });

        return models;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Budget budget) {
        budgetRepository.save(budget);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/budget", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@RequestBody List<Budget> budgets) {
        for(Budget budget : budgets) {
            Budget budgetInDb = budgetRepository.findById(budget.getId()).orElse(null);
            if(budgetInDb == null) {
                throw new NotFoundException();
            } else {
                budgetInDb.setName(budget.getName());
                if(!budget.getIsDistributed()) {
                    budgetInDb.setStartDate(budget.getStartDate());
                    budgetInDb.setEndDate(budget.getEndDate());
                    budgetInDb.setAmountPerMember(budget.getAmountPerMember());
                    budgetInDb.setRules(budget.getRules());
                }
                budgetRepository.save(budgetInDb);
            }
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
