package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class AuthenticationHttpClient extends GenericHttpClient {

    public AuthenticationResponseModel login(String email, String password) {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        post("/api/auth/login", body);
        return context.getGson().fromJson(context.getLastBody(), AuthenticationResponseModel.class);
    }

    public AuthenticationResponseModel refresh() {
        get("/api/auth/refresh");
        return context.getGson().fromJson(context.getLastBody(), AuthenticationResponseModel.class);
    }

}
