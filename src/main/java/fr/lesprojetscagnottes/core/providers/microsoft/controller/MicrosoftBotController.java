package fr.lesprojetscagnottes.core.providers.microsoft.controller;

import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "MicrosoftBot", description = "The Microsoft Bot API")
public class MicrosoftBotController {

    @Autowired
    private MicrosoftBotService microsoftBotService;

    @Operation(summary = "Send an hello world message", description = "Send an hello world message on the Slack workspace", tags = { "Slack" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Team ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Slack Team or organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/ms/{id}/hello", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void hello(Principal principal, @PathVariable Long id) {
        microsoftBotService.hello(principal, id);
    }
}
