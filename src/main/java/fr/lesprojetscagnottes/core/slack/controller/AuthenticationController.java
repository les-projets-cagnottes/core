package fr.lesprojetscagnottes.core.slack.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.budget.Account;
import fr.lesprojetscagnottes.core.budget.Budget;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.common.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.common.exception.InternalServerException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.strings.StringGenerator;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.budget.AccountRepository;
import fr.lesprojetscagnottes.core.budget.BudgetRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.common.security.TokenProvider;
import fr.lesprojetscagnottes.core.common.service.HttpClientService;
import fr.lesprojetscagnottes.core.user.UserService;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.slack.entity.SlackUserEntity;
import fr.lesprojetscagnottes.core.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.slack.repository.SlackUserRepository;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.Set;

@Slf4j
@RestController("slackAuthenticationController")
@RequestMapping("/api")
@Tag(name = "Authentication", description = "The Authentication API")
public class AuthenticationController {

    @Value("${fr.lesprojetscagnottes.slack.client_id}")
    private String slackClientId;

    @Value("${fr.lesprojetscagnottes.slack.client_secret}")
    private String slackClientSecret;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Sign in with Slack", description = "Exchanging a verification code for an access token with Slack", tags = {"Authentication"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "No Slack team has been found", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Unknown error with Slack", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/login/slack", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel slack(@RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        String url = "https://slack.com/api/oauth.v2.access?client_id=" + slackClientId + "&client_secret=" + slackClientSecret + "&code=" + code + "&redirect_uri=" + redirect_uri;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            log.debug("Call {}", url);
            HttpResponse<String> response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from {} : {}", url, response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("authed_user") != null && json.get("team") != null) {
                final SlackTeamEntity slackTeam = slackTeamRepository.findByTeamId(json.get("team").getAsJsonObject().get("id").getAsString());
                if (slackTeam == null) {
                    throw new NotFoundException();
                }

                OrganizationEntity organization = slackTeam.getOrganization();
                if (organization == null) {
                    throw new NotFoundException();
                }

                JsonObject jsonUser = json.get("authed_user").getAsJsonObject();

                // Import SlackUser from Slack API
                SlackUserEntity slackUser = slackUserRepository.findBySlackId(jsonUser.get("id").getAsString());
                if (slackUser == null) {
                    slackUser = slackClientService.getUser(jsonUser.get("access_token").getAsString());
                    slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUser.getSlackId()));
                }
                slackUser.setSlackTeam(slackTeam);

                // Build user from SlackUser
                UserEntity user = userRepository.findByEmail(slackUser.getEmail());
                if (user == null) {
                    user = new UserEntity();
                    user.setCreatedBy("Slack Login");
                    user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
                    user.setFirstname(slackUser.getName());
                    user.setEmail(slackUser.getEmail());
                } else if (user.getPassword().isEmpty()) {
                    user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
                }
                user.setUpdatedBy("Slack Login");
                user.setUsername(slackUser.getEmail());
                user.setAvatarUrl(slackUser.getImage_192());
                final UserEntity userInDb = userRepository.save(UserGenerator.newUser(user));

                // Save SlackUser with link to user
                slackUser.setUser(userInDb);
                final SlackUserEntity slackUserInDb = slackUserRepository.save(slackUser);

                // If the User doesnt have the SlackUser -> Add it
                // Else -> replace by the new one
                userInDb.getSlackUsers().stream().filter(userSlackUser -> userSlackUser.getUser().getId().equals(userInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                userSlackUser -> userSlackUser = slackUserInDb,
                                () -> userInDb.getSlackUsers().add(slackUserInDb));
                userRepository.save(userInDb);

                // If the SlackTeam doesnt have the SlackUser -> Add it
                // Else -> replace by the new one
                slackTeam.getSlackUsers().stream().filter(slackTeamSlackUser -> slackTeamSlackUser.getUser().getId().equals(userInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                slackTeamSlackUser -> slackTeamSlackUser = slackUserInDb,
                                () -> slackTeam.getSlackUsers().add(slackUserInDb));
                slackTeamRepository.save(slackTeam);

                // If the User is not member of organization => Add it
                // Else -> replace by the new one
                organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                member -> member = userInDb,
                                () -> organization.getMembers().add(userInDb)
                        );
                organizationRepository.save(organization);

                Set<Budget> budgets = budgetRepository.findALlByEndDateGreaterThanAndIsDistributedAndAndOrganizationId(new Date(), true, organization.getId());
                budgets.forEach(budget -> {
                    Account account = accountRepository.findByOwnerIdAndBudgetId(userInDb.getId(), budget.getId());
                    if (account == null) {
                        account = new Account();
                        account.setAmount(budget.getAmountPerMember());
                        account.setBudget(budget);
                    }
                    account.setInitialAmount(budget.getAmountPerMember());
                    account.setOwner(userInDb);
                    accountRepository.save(account);
                });

                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, userService.getAuthorities(user.getId()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                final String token = jwtTokenUtil.generateToken(authentication);
                return new AuthenticationResponseModel(token);
            } else {
                throw new InternalServerException();
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
