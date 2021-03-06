package fr.lesprojetscagnottes.core.content.controller;

import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.content.repository.ContentRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

@RequestMapping("/api")
@Tag(name = "Contents", description = "The Contents API")
@RestController
public class ContentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find a content by its ID", description = "Find a content by its ID", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the content", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Content not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/content/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ContentModel getById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get content by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> organizationsContent = organizationRepository.findAllByContents_Id(id);
        Set<OrganizationEntity> organizationsPrincipal = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.hasNoACommonOrganization(organizationsPrincipal, organizationsContent) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get content by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        ContentEntity entity = contentRepository.findById(id).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to get organization by ID : organization not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        return ContentModel.fromEntity(entity);
    }

    @Operation(summary = "Get list of contents by a list of IDs", description = "Find a list of contents by a list of IDs", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the contents", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = ContentModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<ContentModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<OrganizationEntity> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<ContentModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            ContentEntity content = contentRepository.findById(id).orElse(null);
            if(content == null) {
                LOGGER.error("Impossible to get content {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> contentOrganizations = organizationRepository.findAllByContents_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, contentOrganizations) && userLoggedIn_isNotAdmin) {
                LOGGER.error("Impossible to get content {} : principal {} is not in its organization", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(ContentModel.fromEntity(content));
        }

        return models;
    }

    @Operation(summary = "Update a content", description = "Update a content", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content is updated", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Content not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @RequestMapping(value = "/content", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void update(Principal principal, @RequestBody ContentModel contentModel) {

        // Verify that body is complete
        if(contentModel == null || contentModel.getId() < 0 || contentModel.getName() == null) {
            LOGGER.error("Impossible to update content : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that content exists
        ContentEntity content = contentRepository.findById(contentModel.getId()).orElse(null);
        if(content == null) {
            LOGGER.error("Impossible to update content : content {} not found", contentModel.getId());
            throw new NotFoundException();
        }

        // If the content is associated as rules on a budget, only a sponsor can update it
        Long userLoggedInId = userService.get(principal).getId();
        Set<BudgetEntity> budgets = budgetRepository.findAllByRulesId(content.getId());
        LOGGER.debug(budgets.toString());
        Set<OrganizationEntity> organizations = new LinkedHashSet<>();
        budgets.forEach(budget -> organizations.add(budget.getOrganization()));
        LOGGER.debug(organizations.toString());
        boolean isSponsorOfNoneOrganization = true;
        for (OrganizationEntity organization : organizations) {
            isSponsorOfNoneOrganization &= !this.userService.isSponsorOfOrganization(userLoggedInId, organization.getId());
            LOGGER.debug(String.valueOf(isSponsorOfNoneOrganization));
        }
        if(isSponsorOfNoneOrganization && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to update content : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Update content
        content.setName(contentModel.getName());
        content.setValue(contentModel.getValue());

        // Save content
        contentRepository.save(content);
    }

}
