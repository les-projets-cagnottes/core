package fr.thomah.valyou.service;

import fr.thomah.valyou.controller.ProjectController;
import fr.thomah.valyou.model.SlackTeam;
import fr.thomah.valyou.repository.SlackTeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class SlackClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackClientService.class);

    @Autowired
    private HttpClientService httpClientService;

    public void postMessage(SlackTeam slackTeam, String text) {
        String url = "https://slack.com/api/chat.postMessage?token=" + slackTeam.getBotAccessToken();
        String body = "{\"channel\":\"general\", \"text\":\"" + text + "\"}";
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + slackTeam.getBotAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body().toString());
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
