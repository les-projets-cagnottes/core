package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.controller.UserController;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.thomah.valyou.component.CucumberContext.generateId;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommonStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonStepDefinitions.class);

    @Autowired
    private AuthorityRepository authorityRepository;

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
    private UserController userController;

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
            organizationRepository.save(organization);
        });

        projectRepository.deleteAll();
        budgetRepository.deleteAll();
        contentRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        context.reset();
    }

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

    @And("The following organizations are not registered")
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

    @And("The following users are registered")
    public void theFollowingUsersAreRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

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
            user = userRepository.save(user);

            // Save in Test Map
            user.setPassword(columns.get("password"));
            context.getUsers().put(user.getFirstname(), user);
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
            user = userRepository.save(user);

            organization = organizationRepository.findById(organization.getId()).orElse(null);
            if(organization == null) {
                throw new IllegalArgumentException("Organization not found");
            } else {
                organization.getMembers().add(user);
                organizationRepository.save(organization);
            }

            // Save in Test Map
            user.setPassword(columns.get("password"));
            context.getUsers().put(user.getFirstname(), user);

        }
    }

    @And("The following users are not registered")
    public void theFollowingUsersAreNotRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        User user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new User();
            user.setId(generateId());
            user.setUsername(columns.get("email"));
            user.setEmail(columns.get("email"));
            user.setPassword(BCrypt.hashpw(columns.get("password"), BCrypt.gensalt()));
            user.setLastPasswordResetDate(Date.valueOf(LocalDate.now()));
            user.setFirstname(columns.get("firstname"));
            user.setEnabled(true);
            user.getUserAuthorities().add(authorityRepository.findByName(AuthorityName.ROLE_USER));

            // Save in Test Map
            user.setPassword(columns.get("password"));
            context.getUsers().put(user.getFirstname(), user);
        }

    }

    @And("The following users are granted with authorities")
    public void theFollowingUsersAreGrantedWithAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        User user;
        for (Map<String, String> columns : rows) {

            user = context.getUsers().get(columns.get("firstname"));
            final User userFinal = userRepository.findById(user.getId()).orElse(null);
            Authority authority = authorityRepository.findByName(AuthorityName.valueOf(columns.get("authority")));

            assertNotNull(userFinal);
            userFinal.getUserAuthorities().stream().filter(userAuthority -> userAuthority.getName().equals(authority.getName()))
                    .findAny()
                    .ifPresentOrElse(
                            null,
                            () -> userFinal.getUserAuthorities().add(authority)
                    );

            String password = user.getPassword();
            user = userRepository.save(userFinal);
            user.setPassword(password);
            context.getUsers().put(user.getFirstname(), user);
        }
    }
}
