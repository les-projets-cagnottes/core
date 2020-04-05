package fr.thomah.valyou.component;

import fr.thomah.valyou.entity.Budget;
import fr.thomah.valyou.entity.Donation;
import fr.thomah.valyou.entity.model.BudgetModel;
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

    @Autowired
    private CucumberContext context;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private ResponseEntity<Set<Budget>> response;

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
        try {
            response = restTemplate.exchange(endpoint() + "/usable", HttpMethod.GET, entity, responseType);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void getByOrganizationId(Long organizationId) {
        HttpEntity<Donation> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Set<Budget>> responseType = new ParameterizedTypeReference<>() {};
        try {
            response = restTemplate.exchange(endpoint() + "?organizationId=" + organizationId, HttpMethod.GET, entity, responseType);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }

    public ResponseEntity<Set<Budget>> getLastResponse() {
        return response;
    }

    public void create(final Budget budget) {
        HttpEntity<BudgetModel> entity = new HttpEntity<>(BudgetModel.fromEntity(budget), headers);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint(), entity, Void.class);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void save(final Set<Budget> budgets) {
        Set<BudgetModel> budgetModels = new LinkedHashSet<>();
        budgets.forEach(budget -> budgetModels.add(BudgetModel.fromEntity(budget)));
        HttpEntity<Set<BudgetModel>> entity = new HttpEntity<>(budgetModels, headers);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(endpoint(), HttpMethod.PUT, entity, Void.class);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

}
