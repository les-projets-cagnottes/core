package fr.lesprojetscagnottes.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Main", description = "The Main API")
public class MainController {

    @Autowired
    private BuildProperties buildProperties;

    @Operation(summary = "Component Healcheck", description = "Verify if this component is alive", tags = { "Main" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "This component is alive", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void health() {}

    @Operation(summary = "Component version", description = "Return version of this component", tags = { "Main" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return version of this component", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/build", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public BuildProperties version() {
        return buildProperties;
    }

    @Operation(summary = "Get log file for current day", description = "Get log file for current day", tags = { "Main" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get log file for current day", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/logs", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource logs() {
        return new FileSystemResource("logs/core.log");
    }

}
