package fr.thomah.valyou.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.thomah.valyou.entity.SlackTeam;
import fr.thomah.valyou.entity.SlackUser;
import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.entity.model.AuthenticationRequestModel;
import fr.thomah.valyou.entity.model.AuthenticationResponseModel;
import fr.thomah.valyou.entity.model.UserModel;
import fr.thomah.valyou.exception.AuthenticationException;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.InternalServerException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.StringGenerator;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.repository.SlackTeamRepository;
import fr.thomah.valyou.repository.SlackUserRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.security.TokenProvider;
import fr.thomah.valyou.service.HttpClientService;
import fr.thomah.valyou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.Duration;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "The Authentication API")
public class AuthenticationController {

    public static final String TOKEN_HEADER = "Authorization";

    private static final String SLACK_CLIENT_ID = System.getenv("VALYOU_SLACK_CLIENT_ID");
    private static final String SLACK_CLIENT_SECRET = System.getenv("VALYOU_SLACK_CLIENT_SECRET");
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Sign in with email and password", description = "Sign in with email and password", tags = { "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or user", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel login(@RequestBody AuthenticationRequestModel user) throws AuthenticationException {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenUtil.generateToken(authentication);
        return new AuthenticationResponseModel(token);
    }

    @Operation(summary = "Refresh auth token", description = "Refresh auth token for another 5 hours", tags = { "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful refresh", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "400", description = "Token is invalid", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/refresh", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel refresh(HttpServletRequest request) {
        String authToken = request.getHeader(TOKEN_HEADER);
        final String token = authToken.substring(7);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        User user = repository.findByUsername(username);
        if(user == null) {
            user = repository.findByEmail(username);
        }
        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return new AuthenticationResponseModel(refreshedToken);
        } else {
            throw new BadRequestException();
        }
    }

    @Operation(summary = "Sign in with Slack", description = "Exchanging a verification code for an access token with Slack", tags = { "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "No Slack team has been found", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Unknown error with Slack", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/login/slack", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel slack(@RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        String url = "https://slack.com/api/oauth.access?client_id=" + SLACK_CLIENT_ID + "&client_secret=" + SLACK_CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + redirect_uri;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("user") != null && json.get("team") != null) {
                final SlackTeam slackTeam = slackTeamRepository.findByTeamId(json.get("team").getAsJsonObject().get("id").getAsString());
                if(slackTeam != null) {
                    JsonObject jsonUser = json.get("user").getAsJsonObject();
                    User user = repository.findByEmail(jsonUser.get("email").getAsString());
                    if(user == null) {
                        user = new User();
                        user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
                        user.setFirstname(jsonUser.get("name").getAsString());
                        user.setEmail(jsonUser.get("email").getAsString());
                    } else if(user.getPassword().isEmpty()) {
                        user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
                    }
                    user.setUsername(jsonUser.get("email").getAsString());
                    user.setAvatarUrl(jsonUser.get("image_192").getAsString());
                    final User userInDb = repository.save(UserGenerator.newUser(user));

                    String slackuserId = json.get("user_id").getAsString();
                    SlackUser slackUser = slackUserRepository.findBySlackId(slackuserId);
                    if(slackUser == null) {
                        slackUser = new SlackUser();
                        slackUser.setSlackId(slackuserId);
                    }
                    slackUser.setSlackTeam(slackTeam);
                    slackUser.setUser(user);
                    slackUser.setName(jsonUser.get("name").getAsString());
                    slackUser.setImage_192(jsonUser.get("image_192").getAsString());
                    slackUser.setEmail(jsonUser.get("email").getAsString());
                    final SlackUser slackUserInDb = slackUserRepository.save(slackUser);

                    // If the User doesnt have the SlackUser -> Add it
                    // Else -> replace by the new one
                    userInDb.getSlackUsers().stream().filter(userSlackUser -> userSlackUser.getUser().getId().equals(userInDb.getId()))
                            .findAny()
                            .ifPresentOrElse(
                                    userSlackUser -> userSlackUser = slackUserInDb,
                                    () -> userInDb.getSlackUsers().add(slackUserInDb));
                    repository.save(userInDb);

                    // If the SlackTeam doesnt have the SlackUser -> Add it
                    // Else -> replace by the new one
                    slackTeam.getSlackUsers().stream().filter(slackTeamSlackUser -> slackTeamSlackUser.getUser().getId().equals(userInDb.getId()))
                            .findAny()
                            .ifPresentOrElse(
                                    slackTeamSlackUser -> slackTeamSlackUser = slackUserInDb,
                                    () -> slackTeam.getSlackUsers().add(slackUserInDb));
                    slackTeamRepository.save(slackTeam);


                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                            AuthorityUtils.createAuthorityList("ROLE_USER"));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    final String token = jwtTokenUtil.generateToken(authentication);
                    return new AuthenticationResponseModel(token);
                } else {
                    throw new NotFoundException();
                }
            } else {
                throw new InternalServerException();
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Operation(summary = "Get current user", description = "Return the user corresponding to the token", tags = { "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the current user", content = @Content(schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "404", description = "No user has been found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/whoami", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserModel whoami(Principal principal) {
        User user = userService.get(principal);
        if(user == null) {
            throw new NotFoundException();
        } else {
            user.setPassword("");
            return UserModel.fromEntity(user);
        }
    }

}
