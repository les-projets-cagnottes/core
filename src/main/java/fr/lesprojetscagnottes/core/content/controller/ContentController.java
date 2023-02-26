package fr.lesprojetscagnottes.core.content.controller;

import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.content.repository.ContentRepository;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Contents", description = "The Contents API")
@RestController
public class ContentController {

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
            log.error("Impossible to get content by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> organizationsContent = organizationRepository.findAllByContents_Id(id);
        Set<OrganizationEntity> organizationsPrincipal = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.hasNoACommonOrganization(organizationsPrincipal, organizationsContent) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get content by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        ContentEntity entity = contentRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to get organization by ID : organization not found");
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
                log.error("Impossible to get content {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> contentOrganizations = organizationRepository.findAllByContents_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, contentOrganizations) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get content {} : principal {} is not in its organization", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(ContentModel.fromEntity(content));
        }

        return models;
    }

    @Operation(summary = "Create a content for organization", description = "Create a content for organization", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content is created", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContentModel create(Principal principal, @RequestBody ContentModel model) {

        // Verify that body is complete
        if(model == null || model.getName() == null || model.getOrganization() == null || model.getOrganization().getId() <= 0) {
            log.error("Impossible to create content in organization : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Long organizationId = model.getOrganization().getId();
        OrganizationEntity organization = organizationRepository.findById(organizationId).orElse(null);
        if(organization == null) {
            log.error("Impossible to get contents of organizations : organization {} not found", organizationId);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotSponsorOfOrganization(userLoggedInId, organizationId) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to create content in organization : principal is not a sponsor of organization {}", organizationId);
            throw new ForbiddenException();
        }

        // Save content
        ContentEntity content = new ContentEntity();
        content.setName(model.getName());
        content.setValue(model.getValue());
        content.setOrganization(organization);
        return ContentModel.fromEntity(contentRepository.save(content));
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
            log.error("Impossible to update content : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that content exists
        ContentEntity content = contentRepository.findById(contentModel.getId()).orElse(null);
        if(content == null) {
            log.error("Impossible to update content : content {} not found", contentModel.getId());
            throw new NotFoundException();
        }

        // If the content is associated as rules on a budget, only a sponsor can update it
        Long userLoggedInId = userService.get(principal).getId();
        Set<BudgetEntity> budgets = budgetRepository.findAllByRulesId(content.getId());
        log.debug(budgets.toString());
        Set<OrganizationEntity> organizations = new LinkedHashSet<>();
        budgets.forEach(budget -> organizations.add(budget.getOrganization()));
        log.debug(organizations.toString());
        boolean isSponsorOfNoneOrganization = true;
        for (OrganizationEntity organization : organizations) {
            isSponsorOfNoneOrganization &= this.userService.isNotSponsorOfOrganization(userLoggedInId, organization.getId());
            log.debug(String.valueOf(isSponsorOfNoneOrganization));
        }
        if(isSponsorOfNoneOrganization && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to update content : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Update content
        content.setName(contentModel.getName());
        content.setValue(contentModel.getValue());

        // Save content
        contentRepository.save(content);
    }

    @Operation(summary = "Remove a content from an organization", description = "Remove a content from an organization", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content removed", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or Content not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/content/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(Principal principal, @PathVariable long id) {

        // Verify that IDs are corrects
        if(id <= 0) {
            log.error("Impossible to delete content : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that user exists
        ContentEntity content = contentRepository.findById(id).orElse(null);
        if(content == null) {
            log.error("Impossible to delete content : content {} doesnt exist", id);
            throw new NotFoundException();
        }

        // Verify that organization exists
        OrganizationEntity organization;
        if(content.getOrganization() != null) {
            organization = organizationRepository.findById(content.getOrganization().getId()).orElse(null);
            if(organization == null) {
                log.error("Impossible to delete content : organization {} doesnt exist", content.getOrganization().getId());
                throw new NotFoundException();
            }
        } else {
            log.error("Impossible to delete content : organization {} doesnt exist", content.getOrganization().getId());
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotSponsorOfOrganization(userLoggedInId, organization.getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to delete content : principal is not a member of organization {}", organization.getId());
            throw new ForbiddenException();
        }

        // Delete content
        contentRepository.deleteById(id);
    }
}
