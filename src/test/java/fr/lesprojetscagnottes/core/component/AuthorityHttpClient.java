package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.entity.Authority;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class AuthorityHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/authority";

    @LocalServerPort
    private int port;

    @Autowired
    private CucumberContext context;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private ResponseEntity<Set<Authority>> response;

    public AuthorityHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public void getUserAuthority() {
        HttpEntity<Authority> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Set<Authority>> responseType = new ParameterizedTypeReference<>() {};
        try {
            response = restTemplate.exchange(endpoint(), HttpMethod.GET, entity, responseType);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }

    public ResponseEntity<Set<Authority>> getLastResponse() {
        return response;
    }
}
