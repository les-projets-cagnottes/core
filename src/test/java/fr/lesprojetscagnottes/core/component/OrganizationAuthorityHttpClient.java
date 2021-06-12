package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.authorization.model.OrganizationAuthorityModel;
import fr.lesprojetscagnottes.core.user.UserModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class OrganizationAuthorityHttpClient extends GenericHttpClient {

    public void grant(UserModel user, final OrganizationAuthorityModel organizationAuthority) {
        post("api/user/" + user.getId() + "/orgauthorities", context.getGson().toJson(organizationAuthority));
    }

}
