package fr.lesprojetscagnottes.core.slack.controller;

import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import fr.lesprojetscagnottes.core.user.service.UserService;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.slack.model.SlackTeamModel;
import fr.lesprojetscagnottes.core.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.slack.repository.SlackUserRepository;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequestMapping("/api")
@Tag(name = "Team:Slack", description = "The Slack Team API")
@RestController
public class SlackTeamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackTeamController.class);

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

    @Operation(summary = "Find a Slack team by its ID", description = "Find a Slack team by its ID", tags = { "Team:Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the content", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/slack/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SlackTeamModel getById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get Slack team by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        SlackTeamEntity entity = slackTeamRepository.findById(id).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to get Slack team by ID : Slack team not found");
            throw new NotFoundException();
        }

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, entity.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get Slack team by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Transform and return organization
        return SlackTeamModel.fromEntity(entity);
    }


}
