package fr.thomah.valyou.component;

import fr.thomah.valyou.model.OrganizationAuthority;
import fr.thomah.valyou.model.User;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    public int grant(User user, final OrganizationAuthority organizationAuthority) {
        HttpEntity<OrganizationAuthority> entity = new HttpEntity<>(organizationAuthority, headers);

        int statusCode = 0;
        try {
            statusCode = restTemplate.postForEntity(endpoint() + user.getId() + "/orgauthorities", entity, Void.class).getStatusCodeValue();
        } catch (HttpClientErrorException ex) {
            statusCode = ex.getStatusCode().value();
        }

        return statusCode;
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }
}
