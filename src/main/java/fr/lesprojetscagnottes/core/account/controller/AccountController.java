package fr.lesprojetscagnottes.core.account.controller;

import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.account.service.AccountService;
import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Accounts", description = "The Accounts API")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Operation(summary = "Get list of accounts by a list of IDs", description = "Find a list of accounts by a list of IDs", tags = { "Accounts" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the accounts", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<AccountModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {
        return accountService.getByIds(principal, ids);
    }

    @Operation(summary = "Get the account of a user on a budget", description = "Get the account of a user on a budget", tags = { "Accounts" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding account", content = @Content(schema = @Schema(implementation = AccountModel.class))),
            @ApiResponse(responseCode = "400", description = "Params are incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId", "userId"})
    public AccountModel getByBudgetAndUser(Principal principal, @RequestParam("budgetId") Long budgetId, @RequestParam("userId") Long userId) {
        return accountService.getByBudgetAndUser(principal, budgetId, userId);
    }

}
