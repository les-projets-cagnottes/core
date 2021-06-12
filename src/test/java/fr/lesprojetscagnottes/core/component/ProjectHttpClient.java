package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.project.ProjectModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class ProjectHttpClient extends GenericHttpClient {

    public void post(final ProjectModel project) {
        post("/api/project", context.getGson().toJson(project));
    }

}
