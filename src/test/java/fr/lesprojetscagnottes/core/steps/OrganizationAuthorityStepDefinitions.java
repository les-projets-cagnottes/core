package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.OrganizationAuthorityHttpClient;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserModel;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.user.UserRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrganizationAuthorityStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationAuthorityStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private OrganizationAuthorityHttpClient organizationAuthorityHttpClient;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

    @Given("The following users are granted with organization authorities")
    public void theFollowingUsersAreGrantedWithOrganizationAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        OrganizationEntity organization;
        UserEntity user;
        for (Map<String, String> columns : rows) {

            user = context.getUsers().get(columns.get("firstname"));
            final UserEntity userFinal = userRepository.findById(user.getId()).orElse(null);
            organization = context.getOrganizations().get(columns.get("organization"));
            final OrganizationAuthorityName authorityNameFinal = OrganizationAuthorityName.valueOf(columns.get("authority"));
            final OrganizationAuthorityEntity organizationAuthority = organizationAuthorityRepository.findByOrganizationIdAndName(organization.getId(), authorityNameFinal);

            assertNotNull(userFinal);

            userFinal.setUserOrganizationAuthorities(organizationAuthorityRepository.findAllByUsers_Id(userFinal.getId()));
            userFinal.getUserOrganizationAuthorities().stream().filter(authority -> authority.getName().equals(authorityNameFinal))
                    .findAny()
                    .ifPresentOrElse(
                            null,
                            () -> userFinal.getUserOrganizationAuthorities().add(organizationAuthority)
                    );

            String password = user.getPassword();
            user = userRepository.save(userFinal);
            user.setPassword(password);
            context.getUsers().put(user.getFirstname(), user);
        }
    }

    @When("{string} grants following users with organization authorities")
    public void grantsFollowingUsersWithOrganizationAuthorities(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        OrganizationAuthorityEntity organizationAuthorityContext;
        OrganizationAuthorityEntity organizationAuthority;
        OrganizationEntity organizationContext;
        OrganizationEntity organization;
        UserEntity user;
        for (Map<String, String> columns : rows) {

            organizationContext = context.getOrganizations().get(columns.get("organization"));
            user = context.getUsers().get(columns.get("firstname"));
            organizationAuthorityContext = context.getOrganizationAuthorities().get(organizationContext.getName() + columns.get("authority"));

            // Create simplest organization
            organization = new OrganizationEntity();
            organization.setId(organizationContext.getId());

            // Create simplest organization authority
            organizationAuthority = new OrganizationAuthorityEntity();
            organizationAuthority.setId(organizationAuthorityContext.getId());

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponseModel response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Send grant request
            organizationAuthorityHttpClient.setBearerAuth(response.getToken());
            organizationAuthorityHttpClient.grant(UserModel.fromEntity(user), OrganizationAuthorityEntity.fromEntity(organizationAuthority));
        }
    }

    @When("{string} withdraw organization authorities to following users")
    public void withdrawOrganizationAuthoritiesToFollowingUsers(String userFirstname, DataTable table) {
        grantsFollowingUsersWithOrganizationAuthorities(userFirstname, table);
    }

    @Then("Verify that following users have the correct number of organization authorities")
    public void verifyThatFollowingUsersHaveTheCorrectNumberOfOrganizationAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        OrganizationEntity organizationContext;
        UserEntity userContext;
        for (Map<String, String> columns : rows) {
            organizationContext = context.getOrganizations().get(columns.get("organization"));
            userContext = context.getUsers().get(columns.get("firstname"));
            Set<OrganizationAuthorityEntity> authorities = organizationAuthorityRepository.findByOrganizationIdAndUsersId(organizationContext.getId(), userContext.getId());
            assertEquals(columns.get("authorities"), String.valueOf(authorities.size()));
        }
    }

    @Then("Verify that following users are granted with organization authorities")
    public void verifyThatFollowingUsersAreGrantedWithOrganizationAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        OrganizationEntity organizationContext;
        UserEntity userContext;
        for (Map<String, String> columns : rows) {
            organizationContext = context.getOrganizations().get(columns.get("organization"));
            userContext = context.getUsers().get(columns.get("firstname"));
            final String authorityName = columns.get("authority");
            OrganizationAuthorityEntity organizationAuthority = organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organizationContext.getId(), userContext.getId(), OrganizationAuthorityName.valueOf(authorityName));
            assertNotNull(organizationAuthority);
        }
    }

}
