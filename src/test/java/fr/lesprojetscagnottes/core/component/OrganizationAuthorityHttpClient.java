package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.entity.OrganizationAuthority;
import fr.lesprojetscagnottes.core.entity.User;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class OrganizationAuthorityHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/user/";

    @LocalServerPort
    private int port;

    @Autowired
    private CucumberContext context;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    public OrganizationAuthorityHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public void grant(User user, final OrganizationAuthority organizationAuthority) {
        HttpEntity<OrganizationAuthority> entity = new HttpEntity<>(organizationAuthority, headers);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint() + user.getId() + "/orgauthorities", entity, Void.class);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }
}
