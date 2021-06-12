package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class DonationHttpClient extends GenericHttpClient {

    public void create(final DonationModel donation) {
        post("/api/donation", context.getGson().toJson(donation));
    }

}
