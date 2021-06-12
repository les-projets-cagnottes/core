package fr.lesprojetscagnottes.core.authorization.controller;

import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.model.AuthorityModel;
import fr.lesprojetscagnottes.core.authorization.model.OrganizationAuthorityModel;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api")
@Tag(name = "Organization Authorities", description = "The Organization Authorities API")
public class OrganizationAuthorityController {

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find all authorities for current user", description = "Find all authorities for current user", tags = { "Authorities" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all authorities for current user", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuthorityModel.class))))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/orgauthorities", method = RequestMethod.GET)
    public Set<OrganizationAuthorityModel> getUserOrganizationAuthorities(Principal principal) {

        // Get user organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationAuthorityEntity> entities = organizationAuthorityRepository.findAllByUsers_Id(userLoggedInId);

        // Convert all entities to models
        Set<OrganizationAuthorityModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> {
            models.add(OrganizationAuthorityModel.fromEntity(entity));
        });

        return models;
    }

}
