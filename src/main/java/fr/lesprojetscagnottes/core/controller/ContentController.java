package fr.lesprojetscagnottes.core.controller;

import fr.lesprojetscagnottes.core.entity.Content;
import fr.lesprojetscagnottes.core.entity.model.ContentModel;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.repository.ContentRepository;
import fr.lesprojetscagnottes.core.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@Tag(name = "Contents", description = "The Contents API")
@RestController
public class ContentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Create a content", description = "Create a content", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content is created", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody ContentModel contentModel) {

        // Verify that body is complete
        if(contentModel == null || contentModel.getName() == null) {
            LOGGER.error("Impossible to create content : body is incomplete");
            throw new BadRequestException();
        }

        // Save content
        contentRepository.save((Content) contentModel);
    }

    @Operation(summary = "Update a content", description = "Update a content", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content is updated", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Content not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @RequestMapping(value = "/content", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void update(@RequestBody ContentModel contentModel) {

        // Verify that body is complete
        if(contentModel == null || contentModel.getId() < 0 || contentModel.getName() == null) {
            LOGGER.error("Impossible to update content : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that content exists
        Content content = contentRepository.findById(contentModel.getId()).orElse(null);
        if(content == null) {
            LOGGER.error("Impossible to update content : content {} not found", contentModel.getId());
            throw new NotFoundException();
        }

        // Update content
        content.setName(contentModel.getName());
        content.setValue(contentModel.getValue());

        // Save content
        contentRepository.save(content);
    }

}
