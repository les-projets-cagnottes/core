package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class UserStepDefinitions {

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        UserEntity user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new UserEntity();
            user.setUsername(columns.get("email"));
            user.setEmail(columns.get("email"));
            user.setPassword(passwordEncoder.encode(columns.get("password")));
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

        OrganizationEntity organization = context.getOrganizations().get(organizationName);

        UserEntity user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new UserEntity();
            user.setUsername(columns.get("email"));
            user.setEmail(columns.get("email"));
            user.setPassword(passwordEncoder.encode(columns.get("password")));
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

        UserEntity user;
        for (Map<String, String> columns : rows) {

            // Create user
            user = new UserEntity();
            user.setId(CucumberContext.generateId());
            user.setUsername(columns.get("email"));
            user.setEmail(columns.get("email"));
            user.setPassword(passwordEncoder.encode(columns.get("password")));
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

        UserEntity user;
        for (Map<String, String> columns : rows) {

            user = context.getUsers().get(columns.get("firstname"));
            final UserEntity userFinal = userRepository.findById(user.getId()).orElse(null);
            AuthorityEntity authority = authorityRepository.findByName(AuthorityName.valueOf(columns.get("authority")));

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

        OrganizationEntity organization;
        UserEntity user;
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
