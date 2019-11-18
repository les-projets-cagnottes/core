package fr.thomah.valyou.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.thomah.valyou.model.SlackTeam;
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

    public void postMessage(SlackTeam slackTeam, String channel, String text) {
        String url = "https://slack.com/api/chat.postMessage";
        String body = "{\"channel\":\"" + channel + "\", \"text\":\"" + text + "\"}";
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

    public String joinChannel(SlackTeam slackTeam) {
        String url = "https://slack.com/api/channels.join";
        String body = "{\"name\":\"general\"}";
        String channelId = "";
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + slackTeam.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body().toString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body().toString(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                channelId =  json.get("channel").getAsJsonObject().get("id").getAsString();
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return channelId;
    }

    public String inviteInChannel(SlackTeam slackTeam, String channelId) {
        String url = "https://slack.com/api/channels.invite";
        String body = "{\"channel\":\"" + channelId + "\", \"user\": \"" + slackTeam.getBotUserId() + "\"}";
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + slackTeam.getAccessToken())
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
        return "";
    }

    public String openDirectMessageChannel(SlackTeam slackTeam, String slackUserId) {
        String url = "https://slack.com/api/im.open";
        String body = "{\"user\": \"" + slackUserId + "\"}";
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
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body().toString(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                return json.get("channel").getAsJsonObject().get("id").getAsString();
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

}
