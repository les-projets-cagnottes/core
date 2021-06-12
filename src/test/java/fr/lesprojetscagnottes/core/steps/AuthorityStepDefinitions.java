package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.AuthorityHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.authorization.model.AuthorityModel;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class AuthorityStepDefinitions {

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
        AuthenticationResponseModel response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get budgets
        authorityHttpClient.setBearerAuth(response.getToken());
        authorityHttpClient.getUserAuthority();
    }

    @Then("It returns following authorities")
    public void itReturnsFollowingAuthorities(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Set<AuthorityModel> authoritiesReturned = new HashSet<>(Arrays.asList(context.getGson().fromJson(context.getLastBody(), AuthorityModel[].class)));
        Assert.assertNotNull(authoritiesReturned);

        AuthorityEntity authority;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            authority = new AuthorityEntity();
            authority.setName(AuthorityName.valueOf(columns.get("authority")));
            final AuthorityEntity authorityFinal = authority;

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
