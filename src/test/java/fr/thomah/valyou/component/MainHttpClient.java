package fr.thomah.valyou.component;

import fr.thomah.valyou.entity.Donation;
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
public class MainHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    public MainHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public int health() {
        HttpEntity<Donation> entity = new HttpEntity<>(headers);

        int statusCode;
        try {
            statusCode = restTemplate.getForEntity(endpoint() + "/health", Void.class, entity).getStatusCodeValue();
        } catch (HttpClientErrorException ex) {
            statusCode = ex.getStatusCode().value();
        }

        return statusCode;
    }
}
