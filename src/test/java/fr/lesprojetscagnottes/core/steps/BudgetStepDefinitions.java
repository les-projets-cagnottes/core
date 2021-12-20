package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.BudgetHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public class BudgetStepDefinitions {

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private BudgetHttpClient budgetHttpClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CucumberContext context;

    @Given("The following budgets are available")
    public void theFollowingBudgetsAreAvailable(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get dates for budget
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.with(firstDayOfYear());
        LocalDate lastDay = now.with(lastDayOfYear());

        BudgetEntity budget;
        for (Map<String, String> columns : rows) {

            // Create budget
            budget = new BudgetEntity();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setStartDate(Date.valueOf(firstDay));
            budget.setEndDate(Date.valueOf(lastDay));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));
            budget = budgetRepository.save(budget);

            // Save in Test Map
            context.getBudgets().put(budget.getName(), budget);
        }
    }

    @And("The following budgets are passed")
    public void theFollowingBudgetsArePassed(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get dates for budget
        LocalDate lastYear = LocalDate.now().minusYears(1);
        LocalDate firstDay = lastYear.with(firstDayOfYear());
        LocalDate lastDay = lastYear.with(lastDayOfYear());

        BudgetEntity budget;
        for (Map<String, String> columns : rows) {

            // Create budget
            budget = new BudgetEntity();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setStartDate(Date.valueOf(firstDay));
            budget.setEndDate(Date.valueOf(lastDay));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));
            budget = budgetRepository.save(budget);

            // Save in Test Map
            context.getBudgets().put(budget.getName(), budget);
        }
    }

    @When("{string} get budgets for {string} organization")
    public void getBudgetsForOrganization(String userFirstname, String organizationName) {

        // Refresh Token
        authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
        AuthenticationResponseModel response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get budgets
        budgetHttpClient.setBearerAuth(response.getToken());
        budgetHttpClient.getOrganizationBudgets(context.getOrganizations().get(organizationName).getId());
    }

    @When("{string} submits following budgets on current year")
    public void submitsFollowingBudgetsOnCurrentYear(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get dates for budget
        LocalDate lastYear = LocalDate.now();
        LocalDate firstDay = lastYear.with(firstDayOfYear());
        LocalDate lastDay = lastYear.with(lastDayOfYear());

        BudgetEntity budget;
        for (Map<String, String> columns : rows) {
            budget = new BudgetEntity();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setStartDate(Date.valueOf(firstDay));
            budget.setEndDate(Date.valueOf(lastDay));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponseModel response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Create budget
            budgetHttpClient.setBearerAuth(response.getToken());
            budgetHttpClient.create(BudgetModel.fromEntity(budget));
        }
    }

    @When("{string} updates following budgets")
    public void updatesFollowingBudgets(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Set<BudgetModel> budgets = new LinkedHashSet<>();
        BudgetEntity budget;
        for (Map<String, String> columns : rows) {
            budget = context.getBudgets().get(columns.get("name"));
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));
            budgets.add(BudgetModel.fromEntity(budget));
        }

        // Refresh Token
        authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
        AuthenticationResponseModel response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Create budget
        budgetHttpClient.setBearerAuth(response.getToken());
        budgetHttpClient.save(budgets);
    }

    @Then("It returns following budgets")
    public void itReturnsFollowingBudgets(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        System.out.println(context.getLastBody());
        Set<BudgetModel> budgetsReturned = new HashSet<>(Arrays.asList(context.getGson().fromJson(context.getLastBody(), BudgetModel[].class)));
        System.out.println(budgetsReturned);
        Assert.assertNotNull(budgetsReturned);

        BudgetEntity budget;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            budget = new BudgetEntity();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));
            final BudgetEntity budgetFinal = budget;

            budgetsReturned.stream()
                    .filter(budgetReturned -> budgetFinal.getName().equals(budgetReturned.getName()))
                    .filter(budgetReturned -> budgetFinal.getAmountPerMember() == budgetReturned.getAmountPerMember())
                    .filter(budgetReturned -> budgetFinal.getIsDistributed().equals(budgetReturned.getIsDistributed()))
                    .filter(budgetReturned -> budgetFinal.getSponsor().getId().equals(budgetReturned.getSponsor().getId()))
                    .filter(budgetReturned -> budgetFinal.getRules().getId().equals(budgetReturned.getRules().getId()))
                    .filter(budgetReturned -> budgetFinal.getOrganization().getId().equals(budgetReturned.getOrganization().getId()))
                    .findAny()
                    .ifPresentOrElse(
                            budgetsReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, budgetsReturned.size());
    }

    @Then("Following budgets are registered")
    public void followingBudgetsAreRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        List<BudgetEntity> budgetsReturned = budgetRepository.findAll();

        BudgetEntity budget;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            budget = new BudgetEntity();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setOrganization(context.getOrganizations().get(columns.get("organization")));
            budget.setIsDistributed(Boolean.valueOf(columns.get("isDistributed")));
            final BudgetEntity budgetFinal = budget;

            budgetsReturned.stream()
                    .filter(budgetReturned -> budgetFinal.getName().equals(budgetReturned.getName()))
                    .filter(budgetReturned -> budgetFinal.getAmountPerMember() == budgetReturned.getAmountPerMember())
                    .filter(budgetReturned -> budgetFinal.getIsDistributed().equals(budgetReturned.getIsDistributed()))
                    .filter(budgetReturned -> budgetFinal.getSponsor().getId().equals(budgetReturned.getSponsor().getId()))
                    .filter(budgetReturned -> budgetFinal.getRules().getId().equals(budgetReturned.getRules().getId()))
                    .filter(budgetReturned -> budgetFinal.getOrganization().getId().equals(budgetReturned.getOrganization().getId()))
                    .findAny()
                    .ifPresentOrElse(
                            budgetsReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, budgetsReturned.size());
    }

}
