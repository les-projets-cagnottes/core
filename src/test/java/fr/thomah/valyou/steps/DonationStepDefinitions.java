package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.component.DonationHttpClient;
import fr.thomah.valyou.controller.UserController;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;

public class DonationStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private DonationHttpClient donationHttpClient;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

    @And("The following organizations are registered")
    public void theFollowingOrganizationsAreRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        OrganizationAuthority organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new Organization();
            organization.setName(columns.get("name"));
            organization = organizationRepository.save(organization);

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthority(organization, authorityName);
                organizationAuthority = organizationAuthorityRepository.save(organizationAuthority);
                organization.getOrganizationAuthorities().add(organizationAuthority);
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }
            organization = organizationRepository.save(organization);

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

    @And("The following users are members of organization {string}")
    public void theFollowingUsersAreMembersOfOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization = context.getOrganizations().get(organizationName);
        User user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new User();
            user.setUsername(columns.get("email"));
            user.setEmail(columns.get("email"));
            user.setPassword(BCrypt.hashpw(columns.get("password"), BCrypt.gensalt()));
            user.setLastPasswordResetDate(Date.valueOf(LocalDate.now()));
            user.setFirstname(columns.get("firstname"));
            user.setEnabled(true);
            user.getUserAuthorities().add(authorityRepository.findByName(AuthorityName.ROLE_USER));
            user.getOrganizations().add(organization);
            user = userRepository.save(user);

            // Save in Test Map
            user.setPassword(columns.get("password"));
            context.getUsers().put(user.getFirstname(), user);
        }
    }

    @And("The following contents are saved in the organization {string}")
    public void theFollowingContentsAreSavedInTheOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Content content;
        for (Map<String, String> columns : rows) {

            // Create content
            content = new Content();
            content.setName(columns.get("name"));
            content.setValue(columns.get("value"));
            content.getOrganizations().add(context.getOrganizations().get(organizationName));
            content = contentRepository.save(content);

            // Save in Test Map
            context.getContents().put(content.getName(), content);
        }
    }

    @And("The following budgets are available in the organization {string}")
    public void theFollowingBudgetsAreAvailableInTheOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get dates for budget
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.with(firstDayOfYear());
        LocalDate lastDay = now.with(lastDayOfYear());

        Organization organization = context.getOrganizations().get(organizationName);
        Budget budget;
        for (Map<String, String> columns : rows) {

            // Create budget
            budget = new Budget();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(context.getUsers().get(columns.get("sponsor")));
            budget.setRules(context.getContents().get(columns.get("rules")));
            budget.setStartDate(Date.valueOf(firstDay));
            budget.setEndDate(Date.valueOf(lastDay));
            budget.setOrganization(organization);
            budget.setIsDistributed(true);
            budget = budgetRepository.save(budget);

            // Save in Test Map
            context.getBudgets().put(budget.getName(), budget);
        }
    }

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

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotEmpty();

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

            // Refresh Token
            donationHttpClient.setBearerAuth(response.getToken());
            donationHttpClient.post(donation);
        }
    }

    @Then("{string} has {string} donation on the {string} budget")
    public void hasDonationOnTheBudget(String userFirstname, String numberOfDonations, String budgetName) {
        Set<Donation> donations = donationRepository.findAllByContributorIdAndBudgetId(context.getUsers().get(userFirstname).getId(), context.getBudgets().get(budgetName).getId());
        assertThat(String.valueOf(donations.size())).isEqualTo(numberOfDonations);
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

            assertThat(budget.getAmountPerMember() - totalAmount).isEqualTo(Float.parseFloat(columns.get("amount")));
        }
    }

}
