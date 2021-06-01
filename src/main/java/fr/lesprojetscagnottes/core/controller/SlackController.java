package fr.lesprojetscagnottes.core.controller;

import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.service.SlackClientService;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@RequestMapping("/api")
@Tag(name = "Slack", description = "The Slack API")
@RestController
public class SlackController {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackController.class);

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

    public void hello(SlackTeam slackTeam) {
        User orgAdminUser = userRepository.findByEmail(slackTeam.getUpdatedBy());

        Map<String, Object> model = new HashMap<>();
        model.put("URL", WEB_URL);

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
            LOGGER.error("Impossible to send hello world message : Team ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            LOGGER.error("Impossible to send hello world message : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        Organization organization = slackTeam.getOrganization();
        if(organization == null ) {
            LOGGER.error("Impossible to send hello world message : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        // Verify that principal has correct privileges :
        // Principal is owner of the organization OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, organization.getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to send hello world message : principal {} has not enough privileges", userLoggedInId);
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
    public void teamJoin(@PathVariable String teamId, @RequestBody User user) {

        // Fails if Team ID or User is missing
        if(teamId == null || teamId.isEmpty() || user == null || user.getSlackUsers().size() != 1) {
            LOGGER.error("Impossible to register a new member in organization : Team ID is incorrect or body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            LOGGER.error("Impossible to register a new member in organization : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        Organization organization = slackTeam.getOrganization();
        if(organization == null ) {
            LOGGER.error("Impossible to register a new member in organization : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        user.getSlackUsers().forEach(slackUser -> {

            SlackUser slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());
            slackUserInDb.setEmail(slackUser.getEmail());
            slackUserInDb.setSlackTeam(slackTeam);
            slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUser.getSlackId()));
            final SlackUser slackUserFinal = slackUserRepository.save(slackUserInDb);

            // Create User if not exists in DB
            User userInDb = userRepository.findBySlackUsers_Id(slackUserFinal.getId());
            if (userInDb == null) {
                userInDb = UserGenerator.newUser(user);
            }
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setPassword(StringsCommon.EMPTY_STRING);
            final User userInDbFinal = userInDb;

            // If the User doesnt have the SlackUser -> Add it
            userInDbFinal.getSlackUsers().stream().filter(userSlackUser -> userSlackUser.getUser().getId().equals(userInDbFinal.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            userSlackUser -> userSlackUser = slackUserFinal,
                            () -> userInDbFinal.getSlackUsers().add(slackUserFinal));

            final User userWithSlackUser = userRepository.save(userInDbFinal);

            // Complete SlackUser with user saved
            slackUserInDb.setUser(userInDb);
            final SlackUser slackUserFinal2 = slackUserRepository.save(slackUserInDb);

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
            Set<Budget> budgets = budgetRepository.findALlByEndDateGreaterThanAndIsDistributedAndAndOrganizationId(new Date(), true, organization.getId());
            budgets.forEach(budget -> {
                Account account = accountRepository.findByOwnerIdAndBudgetId(userWithSlackUser.getId(), budget.getId());
                if(account == null) {
                    account = new Account();
                    account.setAmount(budget.getAmountPerMember());
                    account.setBudget(budget);
                }
                account.setInitialAmount(budget.getAmountPerMember());
                account.setOwner(userWithSlackUser);
                accountRepository.save(account);
            });

            Map<String, Object> model = new HashMap<>();
            model.put("slackTeamName", slackTeam.getOrganization().getName());
            model.put("URL", WEB_URL);

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
    public void updateUser(@PathVariable String teamId, @RequestBody User user) {

        // Fails if Team ID or User is missing
        if(teamId == null || teamId.isEmpty() || user == null) {
            LOGGER.error("Impossible to update a user in an organization : Team ID is incorrect or body is incomplete");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);

        // Verify that any of references are not null
        if(slackTeam == null ) {
            LOGGER.error("Impossible to update a user in an organization : team {} not found", teamId);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        Organization organization = slackTeam.getOrganization();
        if(organization == null ) {
            LOGGER.error("Impossible to update a user in an organization : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        user.getSlackUsers().stream()
                .findFirst()
                .ifPresent(slackUser -> {
                    SlackUser slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());

                    User userEditted = userRepository.findBySlackUsers_Id(slackUserInDb.getId());
                    userEditted.setEnabled(user.getEnabled());
                    final User userInDb = userRepository.save(userEditted);

                    slackUserInDb.setSlackTeam(slackTeam);
                    slackUserInDb.setUser(user);
                    final SlackUser slackUserFinal = slackUserRepository.save(slackUserInDb);

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
