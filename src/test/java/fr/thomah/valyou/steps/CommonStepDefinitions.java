package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static fr.thomah.valyou.component.CucumberContext.generateId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommonStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

    @Given("Empty database")
    public void emptyDatabase() {
        budgetRepository.findAll().forEach(budget -> {
            budget.setProjects(new LinkedHashSet<>());
            budgetRepository.save(budget);
        });

        organizationRepository.findAll().forEach(organization -> {
            organization.setMembers(new LinkedHashSet<>());
            organization.setProjects(new LinkedHashSet<>());
            organizationRepository.save(organization);
        });

        projectRepository.deleteAll();
        budgetRepository.deleteAll();
        contentRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        context.reset();
    }

    @Given("{string} is logged in")
    public void userIsLoggedIn(String userFirstname) {

        User user = new User();
        user.setEmail(context.getUsers().get(userFirstname).getEmail());
        user.setPassword(context.getUsers().get(userFirstname).getPassword());
        AuthenticationResponse response = authenticationHttpClient.login(user.getEmail(), user.getPassword());

        assertNotNull(response);
        assertFalse(response.getToken().isEmpty());

        context.getAuths().put(userFirstname, response);
    }

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
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }
            organization = organizationRepository.save(organization);

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

    @Given("The following organizations are not registered")
    public void theFollowingOrganizationsAreNotRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        OrganizationAuthority organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new Organization();
            organization.setId(generateId());
            organization.setName(columns.get("name"));

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthority(organization, authorityName);
                organizationAuthority.setId(generateId());
                organization.getOrganizationAuthorities().add(organizationAuthority);
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

}
