package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.component.DonationHttpClient;
import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.DonationRepository;
import fr.thomah.valyou.repository.ProjectRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class DonationStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private DonationHttpClient donationHttpClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CucumberContext context;

    @And("The following campaigns are running")
    public void theFollowingCampaignsAreRunning(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.plusMonths(1);

        Project campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Project();
            campaign.setTitle(columns.get("title"));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setStatus(ProjectStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = projectRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @And("The following campaigns have a deadline reached")
    public void theFollowingCampaignsHaveADeadlineReached(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.minusMonths(1);

        Project campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Project();
            campaign.setTitle(columns.get("title"));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setStatus(ProjectStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = projectRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @And("The following campaigns are associated to the {string} organization")
    public void theFollowingCampaignsAreAssociatedToTheOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Project campaign;
        for (Map<String, String> columns : rows) {

            // Associate project to the organization
            campaign = context.getCampaigns().get(columns.get("title"));
            campaign.getOrganizations().add(context.getOrganizations().get(organizationName));
            campaign = projectRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @And("The following campaigns uses the {string} budget")
    public void theFollowingCampaignsUsesTheBudget(String budgetName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Budget budget = context.getBudgets().get(budgetName);
        for (Map<String, String> columns : rows) {

            // Add campaign to the budget
            budget.getProjects().add(context.getCampaigns().get(columns.get("title")));
            budgetRepository.save(budget);

            // Save in Test Map
            context.getBudgets().put(budget.getName(), budget);
        }
    }

    @And("{string} is logged in")
    public void userIsLoggedIn(String userFirstname) {

        User user = new User();
        user.setEmail(context.getUsers().get(userFirstname).getEmail());
        user.setPassword(context.getUsers().get(userFirstname).getPassword());
        AuthenticationResponse response = authenticationHttpClient.login(user.getEmail(), user.getPassword());

        assertNotNull(response);
        assertFalse(response.getToken().isEmpty());

        context.getAuths().put(userFirstname, response);
    }

    @When("{string} submit the following donations")
    public void submitTheFollowingDonations(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Donation donation;
        for (Map<String, String> columns : rows) {

            // Create donation
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setBudget(context.getBudgets().get(columns.get("budget")));
            donation.setProject(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponse response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            donationHttpClient.setBearerAuth(response.getToken());
            donationHttpClient.post(donation);
        }
    }

    @Then("{string} has {string} donation on the {string} budget")
    public void hasDonationOnTheBudget(String userFirstname, String numberOfDonations, String budgetName) {
        Set<Donation> donations = donationRepository.findAllByContributorIdAndBudgetId(context.getUsers().get(userFirstname).getId(), context.getBudgets().get(budgetName).getId());
        assertEquals(numberOfDonations, String.valueOf(donations.size()));
    }

    @And("Following users have corresponding amount left on budgets")
    public void followingUsersHaveCorrespondingAmountLeftOnBudgets(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Budget budget;
        Set<Donation> donations;
        float totalAmount = 0f;
        for (Map<String, String> columns : rows) {
            budget = context.getBudgets().get(columns.get("budget"));
            donations = donationRepository.findAllByContributorIdAndBudgetId(context.getUsers().get(columns.get("user")).getId(), budget.getId());
            for(Donation donation : donations) {
                totalAmount+= donation.getAmount();
            }

            assertEquals(Float.parseFloat(columns.get("amount")), budget.getAmountPerMember() - totalAmount, 0);
        }
    }

}
