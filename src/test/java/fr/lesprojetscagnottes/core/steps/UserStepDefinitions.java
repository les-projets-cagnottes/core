package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.entity.Authority;
import fr.lesprojetscagnottes.core.entity.AuthorityName;
import fr.lesprojetscagnottes.core.entity.Organization;
import fr.lesprojetscagnottes.core.entity.User;
import fr.lesprojetscagnottes.core.repository.UserRepository;
import fr.lesprojetscagnottes.core.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.repository.OrganizationRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class UserStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserStepDefinitions.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

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
            user.setId(CucumberContext.generateId());
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

    @And("The following users are members of organizations")
    public void theFollowingUsersAreMembersOfOrganizations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        User user;
        for (Map<String, String> columns : rows) {

            user = context.getUsers().get(columns.get("user"));
            user = userRepository.findById(user.getId()).orElse(null);

            organization = context.getOrganizations().get(columns.get("organization"));
            organization = organizationRepository.findById(organization.getId()).orElse(null);

            if(user == null || organization == null) {
                throw new IllegalArgumentException("User or Organization not found");
            } else {
                organization.getMembers().add(user);
                organizationRepository.save(organization);
            }

        }
    }
}
