package fr.lesprojetscagnottes.core.providers.slack.controller;

import fr.lesprojetscagnottes.core.common.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.providers.slack.model.SlackTeamModel;
import fr.lesprojetscagnottes.core.providers.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.providers.slack.service.SlackTeamService;
import fr.lesprojetscagnottes.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.security.Principal;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Team:Slack", description = "The Slack Team API")
@RestController
public class SlackTeamController {

    @Autowired
    private SlackTeamService slackTeamService;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find a Slack team by its ID", description = "Find a Slack team by its ID", tags = {"Team:Slack"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the content", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SlackTeamModel getById(Principal principal, @PathVariable("id") Long id) {
        return slackTeamService.findById(principal, id);
    }

    @Operation(summary = "Create a Slack Team with an Oauth code and redirect URI", description = "Create a Slack Team with an Oauth code and redirect URI", tags = { "Team:Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slack Team created", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, params = {"org_id", "code", "redirect_uri"})
    @ResponseStatus(HttpStatus.CREATED)
    public String create(Principal principal, @RequestParam long org_id, @RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        return slackTeamService.create(principal, org_id, code, redirect_uri);
    }

    @Operation(summary = "Update a Slack team", description = "Update a Slack team", tags = {"Team:Slack"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the content", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public SlackTeamModel update(Principal principal, @RequestBody SlackTeamModel slackTeamModel) {
        return slackTeamService.update(principal, slackTeamModel);
    }

    @Operation(summary = "Disconnect Slack workspace from organization", description = "Disconnect Slack workspace from organization", tags = { "Team:Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slack workspace removed", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or User not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(Principal principal, @PathVariable long id) {
        slackTeamService.delete(principal, id);
    }

    @Operation(summary = "Sync Slack data with organization", description = "Sync Slack data with organization", tags = { "Team:Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slack data synced", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack/{id}/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String sync(Principal principal, @PathVariable long id) throws InterruptedException {
        return slackTeamService.sync(principal, id);
    }


}
