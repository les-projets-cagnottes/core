package fr.lesprojetscagnottes.core.providers.microsoft.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.common.service.HttpClientService;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class MicrosoftGraphService {

    @Value("${fr.lesprojetscagnottes.microsoft.client_id}")
    private String microsoftClientId;

    @Value("${fr.lesprojetscagnottes.microsoft.client_secret}")
    private String microsoftClientSecret;

    @Autowired
    private HttpClientService httpClientService;

    public String token(String tenantId, String scope, String code, String redirect_uri) {

        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
        String token = null;
        try {

            String urlParameters = "client_id=" + microsoftClientId;
            urlParameters+= "&scope=" + scope;
            urlParameters+= "&client_secret=" + microsoftClientSecret;
            if(code != null && code.length() > 0) {
                urlParameters+= "&redirect_uri=" + redirect_uri;
                urlParameters+= "&grant_type=authorization_code";
                urlParameters+= "&code=" + code;
            } else {
                urlParameters+= "&grant_type=client_credentials";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(urlParameters))
                    .build();

            log.debug("Call {}", url);
            log.debug("Body :  {}", urlParameters);
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from {} : {}", url, response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            log.debug("Response converted into json : {}", json);
            if (json.get("access_token") != null) {
                token = json.get("access_token").getAsString();
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return token;
    }

    public MicrosoftUserEntity whoami(String token) {
        String url = "https://graph.microsoft.com/v1.0/me";
        MicrosoftUserEntity msUser = null;
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            log.debug("Call {}", url);
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from {} : {}", url, response.body());

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            log.debug("Response converted into json : {}", json);
            if (json.get("mail") != null) {
                msUser = new MicrosoftUserEntity();
                msUser.setMail(json.get("mail").getAsString());
                msUser.setMsId(json.get("id").getAsString());
                msUser.setGivenName(json.get("givenName").getAsString());
                msUser.setSurname(json.get("surname").getAsString());
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return msUser;
    }

    public MicrosoftTeamEntity getOrganization(String token, String tenantId) {
        String url = "https://graph.microsoft.com/v1.0/organization";
        MicrosoftTeamEntity msTeam = null;
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            log.debug("Call {}", url);
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from {} : {}", url, response.body());

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            log.debug("Response converted into json : {}", json);
            if (json.get("value") != null && json.getAsJsonArray("value").size() > 0) {
                JsonArray organizations = json.getAsJsonArray("value");
                int indexOrg = 0;
                boolean found = false;
                JsonObject organization;
                while(!found && indexOrg < organizations.size()) {
                    organization = organizations.get(indexOrg).getAsJsonObject();
                    found = tenantId.equals(organization.get("id").getAsString());
                    indexOrg++;
                }
                if(found) {
                    msTeam = new MicrosoftTeamEntity();
                    msTeam.setDisplayName(organizations.get(indexOrg - 1).getAsJsonObject().get("displayName").getAsString());
                    msTeam.setTenantId(tenantId);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return msTeam;
    }

    public List<MicrosoftUserEntity> getUsers(String token, String companyFilter) {
        String url = "https://graph.microsoft.com/v1.0/users?$select=id,surname,givenName,mail,companyName";
        List<MicrosoftUserEntity> msUsers = new ArrayList<>();
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            log.debug("Call {}", url);
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from {} : {}", url, response.body());

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            log.debug("Response converted into json : {}", json);
            if (json.get("value") != null && json.getAsJsonArray("value").size() > 0) {
                JsonArray msUsersJson = json.getAsJsonArray("value");
                msUsersJson.forEach(msUsersJsonElement -> {
                    JsonObject msUserJson = msUsersJsonElement.getAsJsonObject();

                    MicrosoftUserEntity msUser = new MicrosoftUserEntity();
                    msUser.setMsId(msUserJson.get("id").getAsString());

                    if(!msUserJson.get("mail").isJsonNull()) {
                        msUser.setMail(msUserJson.get("mail").getAsString());
                    }
                    if(!msUserJson.get("surname").isJsonNull()) {
                        msUser.setSurname(msUserJson.get("surname").getAsString());
                    }
                    if(!msUserJson.get("givenName").isJsonNull()) {
                        msUser.setGivenName(msUserJson.get("givenName").getAsString());
                    }
                    if(!msUserJson.get("companyName").isJsonNull()) {
                        msUser.setCompanyName(msUserJson.get("companyName").getAsString());
                    }

                    if(companyFilter == null || companyFilter.isEmpty() || companyFilter.equals(msUser.getCompanyName())) {
                        log.debug("{} eligible to sync", msUser.getMail());
                        msUsers.add(msUser);
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return msUsers;
    }

}
