package fr.lesprojetscagnottes.core.providers.slack.service;

import fr.lesprojetscagnottes.core.common.service.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class CatcherService {

    @Value("${fr.lesprojetscagnottes.slack.catcher.url}")
    private String catcherUrl;

    @Autowired
    private HttpClientService httpClientService;

    public void sendToken(String token) {
        String url = catcherUrl + "/token";
        String body = "{\"token\":\"" + token + "\"}";
        log.debug("POST " + url);
        log.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("response : " + response.body());
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
