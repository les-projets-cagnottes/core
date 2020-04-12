package fr.thomah.valyou.component;

import fr.thomah.valyou.entity.Donation;
import fr.thomah.valyou.entity.model.DonationModel;
import fr.thomah.valyou.pagination.DataPage;
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

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class DonationHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api/donation";

    @LocalServerPort
    private int port;

    @Autowired
    private CucumberContext context;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private ResponseEntity<?> response;

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

    public void post(final Donation donation) {
        HttpEntity<DonationModel> entity = new HttpEntity<>(DonationModel.fromEntity(donation), headers);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint(), entity, Void.class);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void getByProjectId(long projectId) {
        HttpEntity<DonationModel> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<DataPage<DonationModel>> responseType = new ParameterizedTypeReference<>() {};
        try {
            response = restTemplate.exchange(endpoint() + "?projectId=" + projectId, HttpMethod.GET, entity, responseType);
            context.setLastHttpCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }

    public ResponseEntity<?> getLastResponse() {
        return response;
    }

}
