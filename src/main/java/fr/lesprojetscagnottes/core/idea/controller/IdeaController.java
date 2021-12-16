package fr.lesprojetscagnottes.core.idea.controller;

import fr.lesprojetscagnottes.core.idea.model.IdeaModel;
import fr.lesprojetscagnottes.core.idea.service.IdeaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Ideas", description = "The Ideas API")
public class IdeaController {

    @Autowired
    private IdeaService ideaService;

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
        return ideaService.create(principal, model);
    }

    @Operation(summary = "Update an idea", description = "Update an idea", tags = { "Ideas" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Idea updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/idea", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public IdeaModel update(Principal principal, @RequestBody IdeaModel model) {
        return ideaService.update(principal, model);
    }

}
