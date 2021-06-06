package fr.lesprojetscagnottes.core.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class AuthorityHttpClient extends GenericHttpClient {

    public void getUserAuthority() {
        get("/api/authority");
    }

}
