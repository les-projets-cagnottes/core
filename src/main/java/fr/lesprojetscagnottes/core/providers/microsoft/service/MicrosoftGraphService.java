package fr.lesprojetscagnottes.core.providers.microsoft.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.common.service.HttpClientService;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.repository.MicrosoftUserRepository;
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
public class MicrosoftGraphService {

    @Value("${fr.lesprojetscagnottes.microsoft.tenant_id}")
    private String microsoftTenantId;

    @Value("${fr.lesprojetscagnottes.microsoft.client_id}")
    private String microsoftClientId;

    @Value("${fr.lesprojetscagnottes.microsoft.client_secret}")
    private String microsoftClientSecret;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private MicrosoftUserRepository microsoftUserRepository;

    public MicrosoftUserEntity getMsUserByMail(String mail) {
        return microsoftUserRepository.findByMail(mail);
    }

    public MicrosoftUserEntity saveMsUser(MicrosoftUserEntity entity) {
        return microsoftUserRepository.save(entity);
    }

    public String token(String code, String redirect_uri) {

        String url = "https://login.microsoftonline.com/" + microsoftTenantId + "/oauth2/v2.0/token";
        String token = null;
        try {

            String urlParameters = "client_id=" + microsoftClientId;
            urlParameters+= "&scope=openid+profile+offline_access";
            urlParameters+= "&code=" + code;
            urlParameters+= "&redirect_uri=" + redirect_uri;
            urlParameters+= "&grant_type=authorization_code";
            urlParameters+= "&client_secret=" + microsoftClientSecret;

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
                msUser = this.getMsUserByMail(json.get("mail").getAsString());
                if(msUser == null) {
                    msUser = new MicrosoftUserEntity();
                    msUser.setMail(json.get("mail").getAsString());
                }
                msUser.setAccessToken(token);
                msUser.setMsId(json.get("id").getAsString());
                msUser.setGivenName(json.get("givenName").getAsString());
                msUser.setSurname(json.get("surname").getAsString());
                msUser = this.saveMsUser(msUser);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return msUser;
    }

}
