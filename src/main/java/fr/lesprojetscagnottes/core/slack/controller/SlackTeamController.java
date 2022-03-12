package fr.lesprojetscagnottes.core.slack.controller;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.slack.model.SlackTeamModel;
import fr.lesprojetscagnottes.core.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        // Verify that ID is correct
        if (id <= 0) {
            log.error("Impossible to get Slack team by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        SlackTeamEntity entity = slackTeamRepository.findById(id).orElse(null);
        if (entity == null) {
            log.error("Impossible to get Slack team by ID : Slack team not found");
            throw new NotFoundException();
        }

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Long userLoggedInId = userService.get(principal).getId();
        if (!userService.isMemberOfOrganization(userLoggedInId, entity.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get Slack team by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Transform and return organization
        return SlackTeamModel.fromEntity(entity);
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

        // Verify that ID is correct
        // Fails if any of references are null
        if (slackTeamModel == null || StringUtils.isEmpty(slackTeamModel.getTeamId()) || StringUtils.isEmpty(slackTeamModel.getPublicationChannelId())) {
            if (slackTeamModel != null) {
                log.error("Impossible to update project {} : some references are missing", slackTeamModel.getId());
            } else {
                log.error("Impossible to update a null project");
            }
            throw new BadRequestException();
        }
        // Verify that entity exists
        SlackTeamEntity entity = slackTeamRepository.findByTeamId(slackTeamModel.getTeamId());

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Long userLoggedInId = userService.get(principal).getId();
        if (!userService.isManagerOfOrganization(userLoggedInId, entity.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get Slack team by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        if (entity == null) {
            log.error("Impossible to get Slack team by ID : Slack team not found");
            throw new NotFoundException();
        }

        entity.setPublicationChannelId(slackTeamModel.getPublicationChannelId());
        entity.setTeamName(slackTeamModel.getTeamName());
        // Transform and return organization
        return SlackTeamModel.fromEntity(slackTeamRepository.save(entity));
    }


}
