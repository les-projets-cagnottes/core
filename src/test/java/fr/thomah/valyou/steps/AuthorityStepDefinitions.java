package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.AuthorityHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.entity.AuthenticationResponse;
import fr.thomah.valyou.entity.Authority;
import fr.thomah.valyou.entity.AuthorityName;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthorityStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private AuthorityHttpClient authorityHttpClient;

    @Autowired
    private CucumberContext context;

    @When("{string} get his authorities")
    public void getHisAuthorities(String userFirstname) {

        // Refresh Token
        authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
        AuthenticationResponse response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get budgets
        authorityHttpClient.setBearerAuth(response.getToken());
        authorityHttpClient.getUserAuthority();
    }

    @Then("It returns following authorities")
    public void itReturnsFollowingAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Set<Authority> authoritiesReturned = authorityHttpClient.getLastResponse().getBody();
        Assert.assertNotNull(authoritiesReturned);

        Authority authority;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            authority = new Authority();
            authority.setName(AuthorityName.valueOf(columns.get("authority")));
            final Authority authorityFinal = authority;

            authoritiesReturned.stream()
                    .filter(budgetReturned -> authorityFinal.getName().equals(budgetReturned.getName()))
                    .findAny()
                    .ifPresentOrElse(
                            authoritiesReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, authoritiesReturned.size());
    }
}
