package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.AuthenticationResponse;
import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.entity.model.AuthenticationResponseModel;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.ApiTokenRepository;
import fr.thomah.valyou.security.TokenProvider;
import fr.thomah.valyou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Tokens", description = "The Token management API")
public class ApiTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Operation(summary = "List all user tokens", description = "List all user tokens for current user", tags = { "Tokens" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all user tokens for current user", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuthenticationResponseModel.class)))),
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuthenticationResponseModel> list(Principal principal) {
        User user = userService.get(principal);
        List<AuthenticationResponse> apiTokens = apiTokenRepository.findAllByUserId(user.getId());
        List<AuthenticationResponseModel> authenticationResponses = new ArrayList<>();
        apiTokens.forEach(apiToken -> {
            apiToken.setToken("");
            authenticationResponses.add(AuthenticationResponseModel.fromEntity(apiToken));
        });
        return authenticationResponses;
    }

    @Operation(summary = "Generate a token for current user", description = "Generate a 1-year token for current user", tags = { "Tokens" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the generated token", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class)))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/token", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel generateApiToken(Principal principal) {
        User user = userService.get(principal);
        LOGGER.debug(user.toString());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date nextYear = cal.getTime();

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, userService.getAuthorities(user.getId()));
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtTokenUtil.generateToken(authentication, nextYear));
        authenticationResponse.setExpiration(nextYear);
        authenticationResponse.setUser(user);

        return AuthenticationResponseModel.fromEntity(apiTokenRepository.save(authenticationResponse));
    }


    @Operation(summary = "Delete a token for current user", description = "Delete a 1-year token for current user", tags = { "Tokens" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is deleted", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Token not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/token/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(Principal principal, @PathVariable("id") long id) {
        User user = userService.get(principal);
        AuthenticationResponse apiToken = apiTokenRepository.findByIdAndUserId(id, user.getId());
        if(apiToken == null) {
            throw new NotFoundException();
        } else {
            apiTokenRepository.deleteById(apiToken.getId());
        }
    }
}
