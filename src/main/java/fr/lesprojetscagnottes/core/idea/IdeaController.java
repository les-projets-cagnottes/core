package fr.lesprojetscagnottes.core.idea;

import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.content.entity.TagEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.content.repository.TagRepository;
import fr.lesprojetscagnottes.core.user.UserService;
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
import java.util.Date;
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
        OrganizationEntity organization = organizationRepository.findById(model.getOrganization().getId()).orElse(null);
        Set<TagEntity> tags = tagRepository.findAllByIdIn(model.getTagsRef());

        // Fails if any of references are null
        if(organization == null) {
            LOGGER.error("Impossible to create idea : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, model.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to create idea : principal {} is not member of organization {}", userLoggedInId, model.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Save idea
        IdeaEntity idea = new IdeaEntity();
        idea.setIcon(model.getIcon());
        idea.setShortDescription(model.getShortDescription());
        idea.setLongDescription(model.getLongDescription());
        idea.setHasAnonymousCreator(model.getHasAnonymousCreator());
        idea.setHasLeaderCreator(model.getHasLeaderCreator());
        idea.setOrganization(organization);
        idea.setTags(tags);

        if(idea.getHasAnonymousCreator()) {
            idea.setCreatedBy(StringsCommon.ANONYMOUS);
            idea.setUpdatedBy(StringsCommon.ANONYMOUS);
        } else {
            idea.setCreatedBy(userLoggedIn.getEmail());
            idea.setUpdatedBy(userLoggedIn.getEmail());
            idea.setSubmitter(userLoggedIn);
        }
        idea.setCreatedAt(new Date());
        idea.setUpdatedAt(idea.getCreatedAt());

        return IdeaModel.fromEntity(ideaRepository.save(idea));
    }

    @Operation(summary = "Update an idea", description = "Update an idea", tags = { "Ideas" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Idea updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/idea", method = RequestMethod.PUT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void update(Principal principal, @RequestBody IdeaModel model) {

        // Fails if any of references are null
        if(model == null || model.getId() <= 0 || model.getShortDescription() == null || model.getShortDescription().isEmpty()) {
            if(model != null ) {
                LOGGER.error("Impossible to update idea : some references are missing");
            } else {
                LOGGER.error("Impossible to update a null idea");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        IdeaEntity idea = ideaRepository.getOne(model.getId());
        OrganizationEntity organization = organizationRepository.findById(model.getOrganization().getId()).orElse(null);
        Set<TagEntity> tags = tagRepository.findAllByIdIn(model.getTagsRef());

        // Fails if any of references are null
        if(idea == null || organization == null) {
            LOGGER.error("Impossible to update idea : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is the author
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        if((idea.getSubmitter() == null || !userLoggedInId.equals(idea.getSubmitter().getId())) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to update idea : principal {} is not the author of the idea {}", userLoggedInId, model.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Save idea
        idea.setIcon(model.getIcon());
        idea.setShortDescription(model.getShortDescription());
        idea.setLongDescription(model.getLongDescription());
        idea.setHasAnonymousCreator(model.getHasAnonymousCreator());
        idea.setHasLeaderCreator(model.getHasLeaderCreator());
        idea.setOrganization(organization);
        idea.setTags(tags);
        idea.setUpdatedAt(idea.getCreatedAt());

        if(idea.getHasAnonymousCreator()) {
            idea.setCreatedBy(StringsCommon.ANONYMOUS);
            idea.setUpdatedBy(StringsCommon.ANONYMOUS);
        } else {
            idea.setCreatedBy(userLoggedIn.getEmail());
            idea.setUpdatedBy(userLoggedIn.getEmail());
            idea.setSubmitter(null);
        }

        ideaRepository.save(idea);
    }

}
