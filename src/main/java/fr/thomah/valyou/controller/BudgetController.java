package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.OrganizationGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Set;

@RestController
public class BudgetController {

    @Autowired
    private BudgetRepository repository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<Budget> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"isActive"})
    public Set<Budget> getByIsActive(@RequestParam("isActive") boolean isActive) {
        if(isActive) {
            return repository.findAllByEndDateGreaterThan(new Date());
        } else {
            return repository.findAllByEndDateLessThan(new Date());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"organizationId"})
    public Set<Budget> getByOrganizationId(@RequestParam("organizationId") Long organizationId) {
        return repository.findAllByOrganizationId(organizationId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody Budget budget, Principal owner) {
        repository.save(budget);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@RequestBody List<Budget> budgets) {
        for(Budget budget : budgets) {
            Budget budgetInDb = repository.findById(budget.getId()).orElse(null);
            if(budgetInDb == null) {
                throw new NotFoundException();
            } else {
                budgetInDb.setName(budget.getName());
                budgetInDb.setStartDate(budget.getStartDate());
                budgetInDb.setEndDate(budget.getEndDate());
                budgetInDb.setAmountPerMember(budget.getAmountPerMember());
                repository.save(budgetInDb);
            }
        }
    }

    @RequestMapping(value = "/api/budget/{id}/distribute", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void distribute(@PathVariable("id") Long id) {
        Budget budget = repository.findById(id).orElse(null);
        if(budget == null) {
            throw new NotFoundException();
        } else {
            budget.setDistributed(!budget.getDistributed());
            repository.save(budget);
        }
    }

    @RequestMapping(value = "/api/budget/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id) {
        repository.deleteById(id);
    }

}
