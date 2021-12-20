package fr.lesprojetscagnottes.core.authorization.controller;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.model.AuthorityModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
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
@Tag(name = "Authorities", description = "The Authorities API")
public class AuthorityController {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find all authorities for current user", description = "Find all authorities for current user", tags = { "Authorities" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all authorities for current user", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuthorityModel.class))))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/authority", method = RequestMethod.GET)
    public Set<AuthorityModel> getUserAuthorities(Principal principal) {

        // Get user organizations
        UserEntity user = userService.get(principal);
        Set<AuthorityEntity> entities = authorityRepository.findAllByUsers_Id(user.getId());

        // Convert all entities to models
        Set<AuthorityModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> {
            models.add(AuthorityModel.fromEntity(entity));
        });

        return models;
    }

}
