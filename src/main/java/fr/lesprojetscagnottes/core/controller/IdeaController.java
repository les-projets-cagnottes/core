package fr.lesprojetscagnottes.core.controller;

import fr.lesprojetscagnottes.core.entity.Idea;
import fr.lesprojetscagnottes.core.entity.Organization;
import fr.lesprojetscagnottes.core.entity.Tag;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.model.IdeaModel;
import fr.lesprojetscagnottes.core.repository.IdeaRepository;
import fr.lesprojetscagnottes.core.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.repository.TagRepository;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Ideas", description = "The Ideas API")
public class IdeaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdeaController.class);

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Create an idea", description = "Create an idea", tags = { "Ideas" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Idea created", content = @Content(schema = @Schema(implementation = IdeaModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/idea", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IdeaModel create(Principal principal, @RequestBody IdeaModel model) {

        LOGGER.debug(model.toString());
        LOGGER.debug(model.getHasAnonymousCreator().toString());

        // Fails if any of references are null
        if(model == null || model.getShortDescription() == null || model.getShortDescription().isEmpty()) {
            if(model != null ) {
                LOGGER.error("Impossible to create idea : some references are missing");
            } else {
                LOGGER.error("Impossible to create a null idea");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Organization organization = organizationRepository.findById(model.getOrganization().getId()).orElse(null);
        Set<Tag> tags = tagRepository.findAllByIdIn(model.getTagsRef());

        // Fails if any of references are null
        if(organization == null) {
            LOGGER.error("Impossible to create idea : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, model.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to create idea : principal {} is not member of organization {}", userLoggedInId, model.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Save campaign
        Idea idea = new Idea();
        idea.setShortDescription(model.getShortDescription());
        idea.setLongDescription(model.getLongDescription());
        idea.setHasAnonymousCreator(model.getHasAnonymousCreator());
        idea.setHasLeaderCreator(model.getHasLeaderCreator());
        idea.setOrganization(organization);
        idea.setTags(tags);

        return IdeaModel.fromEntity(ideaRepository.save(idea));
    }

}
