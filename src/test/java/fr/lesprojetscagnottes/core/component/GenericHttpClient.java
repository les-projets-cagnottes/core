package fr.lesprojetscagnottes.core.component;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class GenericHttpClient {

    @LocalServerPort
    private int port;

    @Autowired
    protected CucumberContext context;

    protected WebClient client;

    protected Map<String, String> headers = new HashMap<>();

    @PostConstruct
    public void init() {
        client = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void setBearerAuth(String token) {
        headers.put("Authorization", "Bearer " + token);
    }

    protected void get(String endpoint) {
        WebClient.RequestHeadersSpec<?> request = client.get()
                .uri(endpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        ResponseEntity<String> response = request
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    context.setLastHttpCode(error.statusCode().value());
                    return Mono.empty();
                })
                .toEntity(String.class)
                .block();
        if (response != null) {
            context.setLastHttpCode(response.getStatusCode().value());
            context.setLastBody(response.getBody());
        }
    }

    protected void post(String endpoint, String body) {
        WebClient.RequestHeadersSpec<?> request = client.post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        ResponseEntity<String> response = request
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    context.setLastHttpCode(error.statusCode().value());
                    return Mono.empty();
                })
                .toEntity(String.class)
                .block();
        if (response != null) {
            context.setLastHttpCode(response.getStatusCode().value());
            context.setLastBody(response.getBody());
        }
    }

    protected void put(String endpoint, String body) {
        WebClient.RequestHeadersSpec<?> request = client.put()
                .uri(endpoint)
                .body(BodyInserters.fromValue(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        ResponseEntity<String> response = request
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    context.setLastHttpCode(error.statusCode().value());
                    return Mono.empty();
                })
                .toEntity(String.class)
                .block();
        if (response != null) {
            context.setLastHttpCode(response.getStatusCode().value());
            context.setLastBody(response.getBody());
        }
    }
}
