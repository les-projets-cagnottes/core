package fr.lesprojetscagnottes.core.providers.microsoft.controller;

import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftTeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.security.Principal;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Team:Microsoft", description = "The Microsoft Team API")
@RestController
public class MicrosoftTeamController {

    @Autowired
    private MicrosoftTeamService microsoftTeamService;

    @Operation(summary = "Get MS Teams by its ID", description = "Get a MS Team by its ID", tags = { "Team:Microsoft" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the MS Team", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MicrosoftTeamModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/ms/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MicrosoftTeamModel getById(Principal principal, @PathVariable("id") Long id) {
        return microsoftTeamService.findById(principal, id);
    }

    @Operation(summary = "Create a MS Team", description = "Create a MS Team", tags = { "Team:Microsoft" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MS Team created", content = @Content(schema = @Schema(implementation = MicrosoftTeamModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/ms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MicrosoftTeamModel create(Principal principal, @RequestBody MicrosoftTeamModel msTeam) {
        return microsoftTeamService.save(principal, msTeam);
    }

    @Operation(summary = "Update a MS Team", description = "Update a MS Team", tags = { "Team:Microsoft" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MS Team updated", content = @Content(schema = @Schema(implementation = MicrosoftTeamModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/ms", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public MicrosoftTeamModel update(Principal principal, @RequestBody MicrosoftTeamModel msTeam) {
        return microsoftTeamService.save(principal, msTeam);
    }

    @Operation(summary = "Delete a MS Team by its ID", description = "Delete a MS Team by its ID", tags = { "Team:Microsoft" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MS Team deleted", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "MS Team not found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/team/ms/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void delete(Principal principal, @PathVariable("id") long id) {
        microsoftTeamService.delete(principal, id);
    }

    @Operation(summary = "Sync MS Team with organization", description = "Sync MS Team with organization", tags = { "Team:Microsoft" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MS Team data synced", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/team/ms/{id}/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String sync(Principal principal, @PathVariable long id) throws InterruptedException {
        return microsoftTeamService.sync(principal, id);
    }
}
