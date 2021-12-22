package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.account.repository.AccountRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class AccountStepDefinitions {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CucumberContext context;

    @When("The following accounts are created")
    public void theFollowingAccountsAreCreated(DataTable table) {

        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        AccountEntity account;
        for (Map<String, String> columns : rows) {

            // Create budget
            account = new AccountEntity();
            account.setOwner(context.getUsers().get(columns.get("owner")));
            account.setBudget(context.getBudgets().get(columns.get("budget")));
            account.setAmount(Float.parseFloat(columns.get("amount")));
            account.setInitialAmount(Float.parseFloat(columns.get("initialAmount")));
            account = accountRepository.save(account);

            // Save in Test Map
            context.getAccounts().put(account.getBudget().getName() + "-" + account.getOwner().getFirstname(), account);
        }
    }

}
