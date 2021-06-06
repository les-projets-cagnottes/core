package fr.lesprojetscagnottes.core.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class CampaignHttpClient extends GenericHttpClient {

    public void getDonations(long projectId) {
        get("/api/campaign/" + projectId + "/donations");
    }

}
