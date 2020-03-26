package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.component.OrganizationAuthorityHttpClient;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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

        Organization organization;
        User user;
        for (Map<String, String> columns : rows) {

            user = context.getUsers().get(columns.get("firstname"));
            final User userFinal = userRepository.findById(user.getId()).orElse(null);
            organization = context.getOrganizations().get(columns.get("organization"));
            final OrganizationAuthorityName authorityNameFinal = OrganizationAuthorityName.valueOf(columns.get("authority"));
            final OrganizationAuthority organizationAuthority = organizationAuthorityRepository.findByOrganizationIdAndName(organization.getId(), authorityNameFinal);

            assertThat(userFinal).isNotNull();

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

            Set<OrganizationAuthority> organizationAuthorities = organizationAuthorityRepository.findAllByUsers_Id(user.getId());
            LOGGER.debug("User {} has {} organization authorities", user.getId(), organizationAuthorities.size());
        }

    }

    @When("{string} grants following users with organization authorities")
    public void grantsFollowingUsersWithOrganizationAuthorities(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        OrganizationAuthority organizationAuthorityContext;
        OrganizationAuthority organizationAuthority;
        Organization organizationContext;
        Organization organization;
        User user;
        for (Map<String, String> columns : rows) {

            organizationContext = context.getOrganizations().get(columns.get("organization"));
            user = context.getUsers().get(columns.get("firstname"));
            organizationAuthorityContext = context.getOrganizationAuthorities().get(organizationContext.getName() + columns.get("authority"));

            // Create simplest organization
            organization = new Organization();
            organization.setId(organizationContext.getId());

            // Create simplest organization authority
            organizationAuthority = new OrganizationAuthority();
            organizationAuthority.setId(organizationAuthorityContext.getId());

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponse response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Send grant request
            organizationAuthorityHttpClient.setBearerAuth(response.getToken());
            organizationAuthorityHttpClient.grant(user, organizationAuthority);
        }
    }

    @Then("Verify that following users are granted with organization authorities")
    public void verifyThatFollowingUsersAreGrantedWithOrganizationAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organizationContext;
        User userContext;
        for (Map<String, String> columns : rows) {
            organizationContext = context.getOrganizations().get(columns.get("organization"));
            userContext = context.getUsers().get(columns.get("firstname"));
            final String authorityName = columns.get("authority");
            OrganizationAuthority organizationAuthority = organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organizationContext.getId(), userContext.getId(), OrganizationAuthorityName.valueOf(authorityName));
            assertThat(organizationAuthority).isNotNull();
        }
    }

}
