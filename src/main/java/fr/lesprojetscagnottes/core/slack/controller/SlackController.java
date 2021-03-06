package fr.lesprojetscagnottes.core.slack.controller;

import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.budget.repository.AccountRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.slack.entity.SlackUserEntity;
import fr.lesprojetscagnottes.core.slack.repository.SlackUserRepository;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Slack", description = "The Slack API")
@RestController
public class SlackController {

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public void hello(SlackTeamEntity slackTeam) {
        UserEntity orgAdminUser = userRepository.findByEmail(slackTeam.getUpdatedBy());

        Map<String, Object> model = new HashMap<>();
        model.put("URL", webUrl);

        slackTeam.getSlackUsers().stream()
                .filter(slackUser -> slackUser.getId().equals(orgAdminUser.getId())) // TODO Cannot work because slackUserId != UserId
                .findAny()
                .ifPresentOrElse(
                        slackUser -> model.put("contact", "<@" + slackUser.getSlackId() + ">"),
                        () -> model.put("contact", "*" + orgAdminUser.getFirstname() + " " + orgAdminUser.getLastname() + "*"));

        Context context = new Context();
        context.setVariables(model);
        String slackMessage = templateEngine.process("slack/fr/hello", context);

        slackClientService.inviteBotInConversation(slackTeam);
        slackClientService.postMessage(slackTeam, slackTeam.getPublicationChannelId(), slackMessage);
    }

    @Operation(summary = "Send an hello world message", description = "Send an hello world message on the Slack workspace", tags = { "Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Team ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team or organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/slack/{teamId}/hello", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void hello(Principal principal, @PathVariable String teamId) {

        // Fails if Team ID is missing
        if(teamId == null || teamId.isEmpty()) {
            log.error("Impossible to send hello world message : Team ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeamEntity slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            log.error("Impossible to send hello world message : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        OrganizationEntity organization = slackTeam.getOrganization();
        if(organization == null ) {
            log.error("Impossible to send hello world message : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        // Verify that principal has correct privileges :
        // Principal is owner of the organization OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, organization.getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to send hello world message : principal {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Send message
        hello(slackTeam);
    }

    @Operation(summary = "Register a new member in organization", description = "Register a new member in Slack Team organization", tags = { "Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Team ID is incorrect or body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team or organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/slack/{teamId}/member", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void teamJoin(@PathVariable String teamId, @RequestBody UserEntity user) {

        // Fails if Team ID or User is missing
        if(teamId == null || teamId.isEmpty() || user == null || user.getSlackUsers().size() != 1) {
            log.error("Impossible to register a new member in organization : Team ID is incorrect or body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeamEntity slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            log.error("Impossible to register a new member in organization : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        OrganizationEntity organization = slackTeam.getOrganization();
        if(organization == null ) {
            log.error("Impossible to register a new member in organization : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        user.getSlackUsers().forEach(slackUser -> {

            SlackUserEntity slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());
            slackUserInDb.setEmail(slackUser.getEmail());
            slackUserInDb.setSlackTeam(slackTeam);
            slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUser.getSlackId()));
            final SlackUserEntity slackUserFinal = slackUserRepository.save(slackUserInDb);

            // Create User if not exists in DB
            UserEntity userInDb = userRepository.findBySlackUsers_Id(slackUserFinal.getId());
            if (userInDb == null) {
                userInDb = UserGenerator.newUser(user);
            }
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setPassword(StringsCommon.EMPTY_STRING);
            final UserEntity userInDbFinal = userInDb;

            // If the User doesnt have the SlackUser -> Add it
            userInDbFinal.getSlackUsers().stream().filter(userSlackUser -> userSlackUser.getUser().getId().equals(userInDbFinal.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            userSlackUser -> userSlackUser = slackUserFinal,
                            () -> userInDbFinal.getSlackUsers().add(slackUserFinal));

            final UserEntity userWithSlackUser = userRepository.save(userInDbFinal);

            // Complete SlackUser with user saved
            slackUserInDb.setUser(userInDb);
            final SlackUserEntity slackUserFinal2 = slackUserRepository.save(slackUserInDb);

            // If the SlackTeam doesnt have the SlackUser -> Add it
            slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserFinal2.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            slackTeamUser -> {},
                            () -> {
                                slackTeam.getSlackUsers().add(slackUserFinal2);
                                slackTeamRepository.save(slackTeam);
                            });

            // If the User is not member of organization => Add it
            organization.getMembers().stream().filter(member -> member.getId().equals(userWithSlackUser.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            member -> {},
                            () -> {
                                organization.getMembers().add(userWithSlackUser);
                                organizationRepository.save(organization);
                            }
                    );

            // Distribute usable budgets
            Set<BudgetEntity> budgets = budgetRepository.findALlByEndDateGreaterThanAndIsDistributedAndAndOrganizationId(new Date(), true, organization.getId());
            budgets.forEach(budget -> {
                AccountEntity account = accountRepository.findByOwnerIdAndBudgetId(userWithSlackUser.getId(), budget.getId());
                if(account == null) {
                    account = new AccountEntity();
                    account.setAmount(budget.getAmountPerMember());
                    account.setBudget(budget);
                }
                account.setInitialAmount(budget.getAmountPerMember());
                account.setOwner(userWithSlackUser);
                accountRepository.save(account);
            });

            Map<String, Object> model = new HashMap<>();
            model.put("slackTeamName", slackTeam.getOrganization().getName());
            model.put("URL", webUrl);

            Context context = new Context();
            context.setVariables(model);
            String slackMessage = templateEngine.process("slack/fr/new-member", context);

            slackClientService.postMessage(slackTeam, slackUser.getImId(), slackMessage);
        });
    }

    @Operation(summary = "Update a user in an organization", description = "Update a user in a Slack Team organization", tags = { "Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Team ID is incorrect or body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team or organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/slack/{teamId}/member", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateUser(@PathVariable String teamId, @RequestBody UserEntity user) {

        // Fails if Team ID or User is missing
        if(teamId == null || teamId.isEmpty() || user == null) {
            log.error("Impossible to update a user in an organization : Team ID is incorrect or body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeamEntity slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            log.error("Impossible to update a user in an organization : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        OrganizationEntity organization = slackTeam.getOrganization();
        if(organization == null ) {
            log.error("Impossible to update a user in an organization : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        user.getSlackUsers().stream()
                .findFirst()
                .ifPresent(slackUser -> {
                    SlackUserEntity slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());

                    UserEntity userEditted = userRepository.findBySlackUsers_Id(slackUserInDb.getId());
                    userEditted.setEnabled(user.getEnabled());
                    final UserEntity userInDb = userRepository.save(userEditted);

                    slackUserInDb.setSlackTeam(slackTeam);
                    slackUserInDb.setUser(user);
                    final SlackUserEntity slackUserFinal = slackUserRepository.save(slackUserInDb);

                    if(user.getEnabled()) {
                        slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserFinal.getId()))
                                .findAny()
                                .ifPresentOrElse(
                                        slackTeamUser -> slackTeamUser = slackUserFinal,
                                        () -> slackTeam.getSlackUsers().add(slackUserFinal));
                        slackTeamRepository.save(slackTeam);

                        organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                                .findAny()
                                .ifPresentOrElse(
                                        member -> member = userInDb,
                                        () -> organization.getMembers().add(userInDb)
                                );
                    } else {
                        organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                                .findAny()
                                .ifPresent(member -> organization.getMembers().remove(member));
                    }
                    organizationRepository.save(organization);
                });
    }

}
