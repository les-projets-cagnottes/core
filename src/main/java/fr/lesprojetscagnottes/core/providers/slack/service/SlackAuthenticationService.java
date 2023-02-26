package fr.lesprojetscagnottes.core.providers.slack.service;

import fr.lesprojetscagnottes.core.account.service.AccountService;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.authentication.service.AuthService;
import fr.lesprojetscagnottes.core.common.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.common.exception.UnauthaurizedException;
import fr.lesprojetscagnottes.core.common.security.TokenProvider;
import fr.lesprojetscagnottes.core.common.strings.StringGenerator;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.service.OrganizationService;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackUserEntity;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackAuthenticationService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private SlackTeamService slackTeamService;

    @Autowired
    private SlackUserService slackUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider jwtTokenUtil;

    public AuthenticationResponseModel login(String code, String redirect_uri) throws AuthenticationException {

        // Get token from Slack
        SlackUserEntity slackAuthedUser = slackClientService.token(code, redirect_uri);
        if(slackAuthedUser == null) {
            log.error("Unable to login with Slack : no token retrieved");
            throw new UnauthaurizedException();
        }

        // Get team from Slack
        SlackTeamEntity slackApiTeam = slackClientService.getTeam(slackAuthedUser.getAccessToken());
        if(slackApiTeam == null) {
            log.error("Unable to login with Slack : no team retrieved");
            throw new UnauthaurizedException();
        }

        // Verify Slack Team match in DB
        SlackTeamEntity slackTeam = slackTeamService.findByTeamId(slackApiTeam.getTeamId());
        if(slackTeam == null) {
            log.error("Unable to login with Slack : the team is not registered in DB");
            throw new UnauthaurizedException();
        }

        // Verify Slack Team has an associated organization
        if(slackTeam.getOrganization() == null || slackTeam.getOrganization().getId() <= 0) {
            log.error("Unable to login with Slack : no organization associated");
            throw new UnauthaurizedException();
        }

        // Sync personal infos
        SlackUserEntity slackApiUser = slackClientService.getUser(slackAuthedUser.getAccessToken(), slackAuthedUser.getSlackId());
        if(slackApiUser == null) {
            log.error("Unable to login with Slack : cannot get Slack user");
            throw new UnauthaurizedException();
        }

        // Save Slack User
        SlackUserEntity slackUser = slackUserService.findBySlackId(slackApiUser.getSlackId());
        if(slackUser == null) {
            slackUser = new SlackUserEntity();
            slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackApiUser.getSlackId()));
            slackUser.setUser(null);
        }
        slackUser.setEmail(slackApiUser.getEmail());
        slackUser.setSlackId(slackApiUser.getSlackId());
        slackUser.setName(slackApiUser.getName());
        slackUser.setImage_192(slackApiUser.getImage_192());
        slackUser.setSlackTeam(slackTeam);
        slackUser = slackUserService.save(slackUser);

        // Update corresponding user
        UserEntity user = userService.findByEmail(slackUser.getEmail());
        if(user == null) {
            user = UserGenerator.newUser(slackUser.getEmail(), StringGenerator.randomString());
            user.setFirstname(slackUser.getName());
        } else if (user.getPassword().isEmpty()) {
            user.setPassword(StringGenerator.randomString());
        }
        user.setUsername(slackUser.getEmail());
        user.setAvatarUrl(slackUser.getImage_192());
        UserEntity savedUser = userService.save(user);

        // Associate User & Slack User
        slackUser.setUser(savedUser);
        slackUserService.save(slackUser);

        // If the User is not member of organization => Add it
        // Else -> replace by the new one
        OrganizationEntity organization = organizationService.findById(slackTeam.getOrganization().getId());
        organization.getMembers().stream().filter(member -> member.getId().equals(savedUser.getId()))
                .findAny()
                .ifPresentOrElse(
                        member -> member = savedUser,
                        () -> organization.getMembers().add(savedUser)
                );
        organizationService.save(organization);

        // Create accounts for usable budgets
        accountService.createUserAccountsForUsableBudgets(savedUser, organization.getId());

        // Generate token
        Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser, null, authService.getAuthorities(user.getId()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new AuthenticationResponseModel(jwtTokenUtil.generateToken(authentication));
    }


}
