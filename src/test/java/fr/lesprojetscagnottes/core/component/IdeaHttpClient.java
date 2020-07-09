package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.model.IdeaModel;
import fr.lesprojetscagnottes.core.pagination.DataPage;
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
public class IdeaHttpClient {

    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/api";

    @LocalServerPort
    private int port;

    @Autowired
    private CucumberContext context;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private ResponseEntity<IdeaModel> responseIdea;
    private ResponseEntity<DataPage<IdeaModel>> responseDataPageIdeas;

    public IdeaHttpClient() {
        restTemplate = new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String endpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public void setBearerAuth(String token) {
        headers.setBearerAuth(token);
    }

    public ResponseEntity<IdeaModel> getLastResponse() {
        return responseIdea;
    }

    public ResponseEntity<DataPage<IdeaModel>> getLastDataPage() {
        return responseDataPageIdeas;
    }

    public void create(final IdeaModel idea) {
        HttpEntity<IdeaModel> entity = new HttpEntity<>(idea, headers);
        try {
            responseIdea = restTemplate.postForEntity(endpoint() + "/idea", entity, IdeaModel.class);
            context.setLastHttpCode(responseIdea.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

    public void getByOrganization(Long id, int offset, int limit) {
        HttpEntity<IdeaModel> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<DataPage<IdeaModel>> responseType = new ParameterizedTypeReference<>() {};
        try {
            String url = endpoint() + "/organization/" + id + "/ideas" + "?offset=" + offset + "&limit=" + limit;
            responseDataPageIdeas = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            context.setLastHttpCode(responseDataPageIdeas.getStatusCodeValue());
        } catch (HttpClientErrorException ex) {
            context.setLastHttpCode(ex.getStatusCode().value());
        }
    }

}
