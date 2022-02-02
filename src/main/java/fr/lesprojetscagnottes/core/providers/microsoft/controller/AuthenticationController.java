package fr.lesprojetscagnottes.core.providers.microsoft.controller;

import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.common.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.common.exception.UnauthaurizedException;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftGraphService;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftTeamService;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftUserService;
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

    @Autowired
    private MicrosoftTeamService microsoftTeamService;

    @Autowired
    private MicrosoftUserService microsoftUserService;

    @Operation(summary = "Sign in with Microsoft", description = "Exchanging a verification code for an access token with Microsoft", tags = {"Authentication"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = AuthenticationResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "No Slack team has been found", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Unknown error with Slack", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/auth/login/microsoft", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponseModel login(@RequestParam String code, @RequestParam("redirect_uri") String redirectUri, @RequestParam("tenant_id") String tenantId) throws AuthenticationException {

        // Get token from Microsoft
        String token = microsoftGraphService.token(tenantId, "openid+profile+offline_access", code, redirectUri);
        if(token == null) {
            log.error("Unable to login with Microsoft : no token retrieved");
            throw new UnauthaurizedException();
        }

        // Get a matching MS Team in DB
        MicrosoftTeamEntity msTeam = microsoftTeamService.findByTenantId(tenantId);
        if(msTeam == null) {
            log.error("Unable to login with Microsoft : no matching team in DB");
            throw new UnauthaurizedException();
        }

        // Get Microsoft Organization
        MicrosoftTeamEntity msGraphTeam = microsoftGraphService.getOrganization(token, tenantId);
        if(msGraphTeam == null) {
            log.error("Unable to login with Microsoft : no Microsoft organization found");
            throw new UnauthaurizedException();
        }

        // Verify Microsoft Organization match MS Team in DB
        if(!msGraphTeam.getTenantId().equals(msTeam.getTenantId())) {
            log.error("Unable to login with Microsoft : Microsoft organization does not match team {} in DB", msTeam.getId());
            throw new UnauthaurizedException();
        }

        // Verify MS Team has an associated organization
        if(msTeam.getOrganization() != null && msTeam.getOrganization().getId() > 0) {
            log.error("Unable to login with Microsoft : no organization associated");
            throw new UnauthaurizedException();
        }

        // Sync personal infos
        MicrosoftUserEntity msGraphUser = microsoftGraphService.whoami(token);
        if(msGraphUser == null) {
            log.error("Unable to login with Microsoft : cannot get Microsoft user");
            throw new UnauthaurizedException();
        }

        MicrosoftUserEntity msUser = microsoftUserService.getByMsId(msGraphUser.getMsId());
        if(msUser == null) {
            msUser = new MicrosoftUserEntity();
        }
        msUser.setAccessToken(msGraphUser.getAccessToken());
        msUser.setMail(msGraphUser.getMail());
        msUser.setMsId(msGraphUser.getMsId());
        msUser.setGivenName(msGraphUser.getGivenName());
        msUser.setSurname(msGraphUser.getSurname());
        msUser.setMsTeam(msTeam);
        msUser = microsoftUserService.save(msUser);

        return null;
    }

}
