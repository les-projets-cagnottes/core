package fr.lesprojetscagnottes.core.providers.microsoft.controller;

import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.common.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("microsoftAuthenticationController")
@RequestMapping("/api")
@Tag(name = "Authentication", description = "The Authentication API")
public class AuthenticationController {

    @Autowired
    private MicrosoftGraphService microsoftGraphService;

    @Operation(summary = "Sign in with Slack", description = "Exchanging a verification code for an access token with Slack", tags = {"Authentication"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "No Slack team has been found", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Unknown error with Slack", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/login/microsoft", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel login(@RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        String token = microsoftGraphService.token(code, redirect_uri);
        return null;
    }

}
