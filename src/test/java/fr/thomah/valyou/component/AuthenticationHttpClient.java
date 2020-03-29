package fr.thomah.valyou.component;

import fr.thomah.valyou.entity.AuthenticationResponse;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class AuthenticationHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHttpClient.class);

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/auth";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    public AuthenticationHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public AuthenticationResponse login(String email, String password) {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(endpoint() + "/login", request, AuthenticationResponse.class).getBody();
    }

    public AuthenticationResponse refresh() {
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<AuthenticationResponse> result = restTemplate.exchange(endpoint() + "/refresh", HttpMethod.GET, request, AuthenticationResponse.class);
        return result.getBody();
    }

    public void setBearerAuth(String token) {
        headers.set("Authorization", "Bearer " + token);
    }
}
