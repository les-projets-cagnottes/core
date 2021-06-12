package fr.lesprojetscagnottes.core.steps;

import com.google.gson.reflect.TypeToken;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.IdeaHttpClient;
import fr.lesprojetscagnottes.core.idea.IdeaEntity;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.idea.IdeaModel;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.idea.IdeaRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class IdeaStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdeaStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private IdeaHttpClient ideaHttpClient;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private CucumberContext context;

    @And("The following ideas are submitted")
    public void theFollowingIdeasAreSubmitted(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        IdeaEntity idea;
        for (Map<String, String> columns : rows) {
            idea = new IdeaEntity();
            idea.setShortDescription(columns.get("shortDescription"));
            idea.setLongDescription(columns.get("longDescription"));
            idea.setHasAnonymousCreator(Boolean.valueOf(columns.get("hasAnonymousCreator")));
            idea.setHasLeaderCreator(Boolean.valueOf(columns.get("hasLeaderCreator")));
            idea.setOrganization(context.getOrganizations().get(columns.get("organization")));
            idea = ideaRepository.save(idea);

            // Save in Test Map
            context.getIdeas().put(idea.getShortDescription(), idea);
        }
    }

    @And("{string} submit the following ideas")
    public void submitTheFollowingIdeas(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        IdeaModel idea;
        for (Map<String, String> columns : rows) {
            idea = new IdeaModel();
            idea.setShortDescription(columns.get("shortDescription"));
            idea.setLongDescription(columns.get("longDescription"));
            idea.setHasAnonymousCreator(Boolean.valueOf(columns.get("hasAnonymousCreator")));
            idea.setHasLeaderCreator(Boolean.valueOf(columns.get("hasLeaderCreator")));
            idea.setOrganization(new GenericModel(context.getOrganizations().get(columns.get("organization"))));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponseModel response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            ideaHttpClient.setBearerAuth(response.getToken());
            ideaHttpClient.create(idea);
        }
    }

    @And("{string} gets ideas of the {string} organization")
    public void getsIdeasOfTheOrganization(String userFirstname, String organizationName) {

        // Refresh Token
        authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
        AuthenticationResponseModel response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get donations
        ideaHttpClient.setBearerAuth(response.getToken());
        ideaHttpClient.getByOrganization(context.getOrganizations().get(organizationName).getId(), 0, 10);
    }

    @And("It returns following ideas")
    public void itReturnsFollowingIdeas(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Type dataPageType = new TypeToken<DataPage<IdeaModel>>(){}.getType();
        DataPage<IdeaModel> body = context.getGson().fromJson(context.getLastBody(), dataPageType);
        Assert.assertNotNull(body);
        List<IdeaModel> ideasReturned = body.getContent();
        Assert.assertNotNull(ideasReturned);

        IdeaEntity idea;
        for (Map<String, String> columns : rows) {

            idea = new IdeaEntity();
            idea.setShortDescription(columns.get("shortDescription"));
            idea.setLongDescription(columns.get("longDescription"));
            idea.setHasAnonymousCreator(Boolean.valueOf(columns.get("hasAnonymousCreator")));
            idea.setHasLeaderCreator(Boolean.valueOf(columns.get("hasLeaderCreator")));
            idea.setOrganization(context.getOrganizations().get(columns.get("organization")));
            final IdeaEntity ideaFinal = idea;

            ideasReturned.stream()
                    .filter(ideaReturned -> ideaFinal.getShortDescription().equals(ideaReturned.getShortDescription()))
                    .filter(ideaReturned -> ideaFinal.getLongDescription().equals(ideaReturned.getLongDescription()))
                    .filter(ideaReturned -> ideaFinal.getHasAnonymousCreator().equals(ideaReturned.getHasAnonymousCreator()))
                    .filter(ideaReturned -> ideaFinal.getHasLeaderCreator().equals(ideaReturned.getHasLeaderCreator()))
                    .filter(ideaReturned -> ideaFinal.getOrganization().getId().equals(ideaReturned.getOrganization().getId()))
                    .findAny()
                    .ifPresentOrElse(
                            ideasReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, ideasReturned.size());
    }

    @And("Last response is anonymized")
    public void lastResponseIsAnonymized() {
        IdeaModel ideaReturned = context.getGson().fromJson(context.getLastBody(), IdeaModel.class);
        Assert.assertNotNull(ideaReturned);
        Assert.assertEquals(StringsCommon.ANONYMOUS, ideaReturned.getCreatedBy());
    }
}
