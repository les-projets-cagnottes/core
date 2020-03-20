package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.DonationHttpClient;
import fr.thomah.valyou.controller.AuthenticationController;
import fr.thomah.valyou.controller.UserController;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DonationStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private DonationHttpClient donationHttpClient;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    private Map<String, Organization> organizations = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Content> contents = new HashMap<>();
    private Map<String, Budget> budgets = new HashMap<>();

    @Given("The following organizations are registered")
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
            }
            organization = organizationRepository.save(organization);

            // Save in Test Map
            organizations.put(organization.getName(), organization);
        }
    }

    @And("The following users are members of organization {string}")
    public void theFollowingUsersAreMembersOfOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization = organizations.get(organizationName);
        User user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new User();
            user.setEmail(columns.get("email"));
            user.setPassword(BCrypt.hashpw(columns.get("password"), BCrypt.gensalt()));
            user.setFirstname(columns.get("firstname"));
            user.setEnabled(true);
            user.getUserAuthorities().add(authorityRepository.findByName(AuthorityName.ROLE_USER));
            user.getUserOrganizationAuthorities().add(organizationAuthorityRepository.findByOrganizationAndName(organization, OrganizationAuthorityName.ROLE_MEMBER));
            user.getOrganizations().add(organization);
            user = userRepository.save(user);

            // Save in Test Map
            user.setPassword(columns.get("password"));
            users.put(user.getFirstname(), user);
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
            content.getOrganizations().add(organizations.get(organizationName));
            content = contentRepository.save(content);

            // Save in Test Map
            contents.put(content.getName(), content);
        }
    }

    @And("The following pots are available in the organization {string}")
    public void theFollowingPotsAreAvailableInTheOrganization(String organizationName, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get dates for budget
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.with(firstDayOfYear());
        LocalDate lastDay = now.with(lastDayOfYear());

        Organization organization = organizations.get(organizationName);
        Budget budget;
        for (Map<String, String> columns : rows) {

            // Create budget
            budget = new Budget();
            budget.setName(columns.get("name"));
            budget.setAmountPerMember(Float.parseFloat(columns.get("amountPerMember")));
            budget.setSponsor(users.get(columns.get("sponsor")));
            budget.setRules(contents.get(columns.get("rules")));
            budget.setStartDate(Date.valueOf(firstDay));
            budget.setEndDate(Date.valueOf(lastDay));
            budget.setOrganization(organization);
            budget.setIsDistributed(true);
            budgetRepository.save(budget);

            // Save in Test Map
            budgets.put(budget.getName(), budget);
        }
    }

    @And("{string} is logged in")
    public void userIsLoggedIn(String userFirstname) {

        User user = new User();
        user.setEmail(users.get(userFirstname).getEmail());
        user.setPassword(users.get(userFirstname).getPassword());
        AuthenticationResponse response = authenticationHttpClient.login(user.getEmail(), user.getPassword());

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotEmpty();

        authenticationHttpClient.setBearerAuth(response.getToken());
        donationHttpClient.setBearerAuth(response.getToken());
    }

    @When("{string} submit the following donations on a non-existing project")
    public void submitTheFollowingDonationsOnANonExistingProject(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Create a non saved project
        Project nonExistingProject = new Project();
        nonExistingProject.setId(0L);

        // Create the simplest user for ID reference
        User user = new User();
        user.setId(users.get(userFirstname).getId());

        Donation donation;
        for (Map<String, String> columns : rows) {

            // Create donation
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setBudget(budgets.get(columns.get("budget")));
            donation.setContributor(user);
            donation.setProject(nonExistingProject);

            // Submit donation
            AuthenticationResponse response = authenticationHttpClient.refresh();
            donationHttpClient.setBearerAuth(response.getToken());
            int statusCode = donationHttpClient.post(donation);

            assertThat(statusCode).isEqualTo(HttpStatus.SC_NOT_FOUND);
        }
    }

    @Then("{string} have {string} donation on the {string} budget")
    public void haveDonationOnTheBudget(String userFirstname, String numberOfDonations, String budgetName) {
        Set<Donation> donations = donationRepository.findAllByContributorIdAndBudgetId(users.get(userFirstname).getId(), budgets.get(budgetName).getId());
        assertThat(numberOfDonations).isEqualTo(String.valueOf(donations.size()));
    }

}
