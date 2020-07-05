package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.IdeaHttpClient;
import fr.lesprojetscagnottes.core.entity.AuthenticationResponse;
import fr.lesprojetscagnottes.core.entity.Idea;
import fr.lesprojetscagnottes.core.repository.ContentRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class IdeaStepDefinitions {

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private IdeaHttpClient ideaHttpClient;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CucumberContext context;

    @And("{string} submit the following ideas")
    public void submitTheFollowingDonations(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Idea idea;
        for (Map<String, String> columns : rows) {

            // Create donation
            idea = new Idea();
            idea.setShortDescription(columns.get("shortDescription"));
            idea.setLongDescription(columns.get("longDescription"));
            idea.setOrganization(context.getOrganizations().get(columns.get("organization")));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponse response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            ideaHttpClient.setBearerAuth(response.getToken());
            ideaHttpClient.create(idea);
        }
    }


}
