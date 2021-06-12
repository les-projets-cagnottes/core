package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.idea.IdeaModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class IdeaHttpClient extends GenericHttpClient {

    public void create(final IdeaModel idea) {
        post("/api/idea", context.getGson().toJson(idea));
    }

    public void getByOrganization(Long id, int offset, int limit) {
        get("/api/organization/" + id + "/ideas" + "?offset=" + offset + "&limit=" + limit);
    }

}
