package fr.lesprojetscagnottes.core.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.SlackTeam;
import fr.lesprojetscagnottes.core.entity.SlackUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlackClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackClientService.class);

    @Autowired
    private HttpClientService httpClientService;

    public void postMessage(SlackTeam slackTeam, String channelId, String text) {
        String url = "https://slack.com/api/chat.postMessage";
        String body = "{\"channel\":\"" + channelId + "\", \"text\":\"" + text + "\"}";
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

    public void inviteBotInConversation(SlackTeam slackTeam) {
        String url = "https://slack.com/api/conversations.invite";
        String body = "{\"channel\":\"" + slackTeam.getPublicationChannelId() + "\", \"users\": \"" + slackTeam.getBotUserId() + "\"}";
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
    }

    public SlackUser getUser(String token) {
        String url = "https://slack.com/api/users.identity";
        LOGGER.debug("GET " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .build();
        HttpResponse<String> response;
        SlackUser slackUser = null;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                JsonObject jsonUser = json.get("user").getAsJsonObject();
                slackUser = new SlackUser();
                slackUser.setSlackId(jsonUser.get("id").getAsString());
                slackUser.setName(jsonUser.get("name").getAsString());
                slackUser.setEmail(jsonUser.get("email").getAsString());
                slackUser.setImage_192(jsonUser.get("image_192").getAsString());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return slackUser;
    }

    public List<SlackUser> listUsers(SlackTeam slackTeam) {
        String url = "https://slack.com/api/users.list";
        String body = "{}";
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
        List<SlackUser> slackUsers = new ArrayList<>();
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body().toString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body().toString(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {

                JsonArray membersJsonArray = json.get("members").getAsJsonArray();
                membersJsonArray.forEach(memberJsonElement -> {
                    JsonObject memberJson = memberJsonElement.getAsJsonObject();

                    SlackUser slackUser = new SlackUser();
                    slackUser.setSlackId(memberJson.get("id").getAsString());
                    slackUser.setDeleted(memberJson.get("deleted").getAsBoolean());

                    JsonElement isRestricted = memberJson.get("is_restricted");
                    if(isRestricted != null)
                        slackUser.setIsRestricted(isRestricted.getAsBoolean());

                    memberJson = memberJson.get("profile").getAsJsonObject();
                    if(memberJson.get("email") != null) {
                        slackUser.setEmail(memberJson.get("email").getAsString());
                        slackUser.setName(memberJson.get("real_name").getAsString());
                        slackUser.setImage_192(memberJson.get("image_192").getAsString());

                        slackUsers.add(slackUser);
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return slackUsers;
    }

    public String openDirectMessageChannel(SlackTeam slackTeam, String slackUserId) {
        String url = "https://slack.com/api/conversations.open";
        String body = "{\"users\": \"" + slackUserId + "\"}";
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
        return StringsCommon.EMPTY_STRING;
    }

}
