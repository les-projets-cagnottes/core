package fr.thomah.valyou.component;

import fr.thomah.valyou.entity.Budget;
import fr.thomah.valyou.entity.Donation;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class BudgetHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/budget";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    private Set<Budget> lastBody = new LinkedHashSet<>();

    public BudgetHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public void getUsableBudgets() {
        HttpEntity<Donation> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Set<Budget>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Set<Budget>> resp = restTemplate.exchange(endpoint() + "/usable", HttpMethod.GET, entity, responseType);
        this.lastBody = resp.getBody();
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }

    public Set<Budget> getLastBody() {
        return lastBody;
    }
}
