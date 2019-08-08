package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.model.Budget;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BudgetController {

    @Autowired
    private BudgetRepository repository;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/budget", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<Budget> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
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
                budgetInDb.setAmountPerMember(budget.getAmountPerMember());
                repository.save(budgetInDb);
            }
        }
    }

}
