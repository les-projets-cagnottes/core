package fr.lesprojetscagnottes.core.providers.slack.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.common.service.HttpClientService;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SlackClientService {

    @Autowired
    private HttpClientService httpClientService;

    public void postMessage(SlackTeamEntity slackTeam, String channelId, String text) {
        String url = "https://slack.com/api/chat.postMessage";
        String body = "{\"channel\":\"" + channelId + "\", \"text\":\"" + text.replaceAll("(\\r\\n|\\n)", "\\\\n") + "\"}";
        log.debug("POST " + url);
        log.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + slackTeam.getBotAccessToken())
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

    public void inviteBotInConversation(SlackTeamEntity slackTeam) {
        String url = "https://slack.com/api/conversations.invite";
        String body = "{\"channel\":\"" + slackTeam.getPublicationChannelId() + "\", \"users\": \"" + slackTeam.getBotUserId() + "\"}";
        log.debug("POST " + url);
        log.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + slackTeam.getAccessToken())
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

    public SlackUserEntity getUser(String token) {
        String url = "https://slack.com/api/users.identity";
        log.debug("GET " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + token)
                .build();
        HttpResponse<String> response;
        SlackUserEntity slackUser = null;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("response : " + response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                JsonObject jsonUser = json.get("user").getAsJsonObject();
                slackUser = new SlackUserEntity();
                slackUser.setSlackId(jsonUser.get("id").getAsString());
                slackUser.setName(jsonUser.get("name").getAsString());
                slackUser.setEmail(jsonUser.get("email").getAsString());
                slackUser.setImage_192(jsonUser.get("image_192").getAsString());
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return slackUser;
    }

    public List<SlackUserEntity> listUsers(SlackTeamEntity slackTeam) {
        String url = "https://slack.com/api/users.list";
        String body = "{}";
        log.debug("POST " + url);
        log.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + slackTeam.getBotAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        List<SlackUserEntity> slackUsers = new ArrayList<>();
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("response : " + response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {

                JsonArray membersJsonArray = json.get("members").getAsJsonArray();
                membersJsonArray.forEach(memberJsonElement -> {
                    JsonObject memberJson = memberJsonElement.getAsJsonObject();

                    SlackUserEntity slackUser = new SlackUserEntity();
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
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return slackUsers;
    }

    public String openDirectMessageChannel(SlackTeamEntity slackTeam, String slackUserId) {
        String url = "https://slack.com/api/conversations.open";
        String body = "{\"users\": \"" + slackUserId + "\"}";
        log.debug("POST " + url);
        log.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + slackTeam.getBotAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("response : " + response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                return json.get("channel").getAsJsonObject().get("id").getAsString();
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return StringsCommon.EMPTY_STRING;
    }

}
