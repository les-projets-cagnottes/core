package fr.thomah.valyou.component;

import fr.thomah.valyou.model.Donation;
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
public class DonationHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/donation";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    public DonationHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public int post(final Donation donation) {
        HttpEntity<Donation> entity = new HttpEntity<>(donation, headers);

        int statusCode = 0;
        try {
            statusCode = restTemplate.postForEntity(endpoint(), entity, Void.class).getStatusCodeValue();
        } catch (HttpClientErrorException ex) {
            statusCode = ex.getStatusCode().value();
        }

        return statusCode;
    }

    public Donation getContents() {
        return restTemplate.getForEntity(endpoint(), Donation.class).getBody();
    }

    public void clean() {
        restTemplate.delete(endpoint());
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }
}
