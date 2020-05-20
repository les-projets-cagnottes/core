package fr.lesprojetscagnottes.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.exception.AuthenticationException;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.generator.StringGenerator;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.model.*;
import fr.lesprojetscagnottes.core.pagination.DataPage;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.service.HttpClientService;
import fr.lesprojetscagnottes.core.service.SlackClientService;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

@RequestMapping("/api")
@Tag(name = "Organizations", description = "The Organizations API")
@RestController
public class OrganizationController {

    private static final String SLACK_CLIENT_ID = System.getenv("LPC_SLACK_CLIENT_ID");
    private static final String SLACK_CLIENT_SECRET = System.getenv("LPC_SLACK_CLIENT_SECRET");

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    private SlackController slackController;

    @Autowired
    private UserController userController;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find all organizations paginated", description = "Find all organizations paginated", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all organizations paginated", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = DataPage.class)))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<OrganizationModel> list(Principal principal, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(offset, limit);

        // If user is not admin => organizations where principal is member
        // Else => all organizations
        Page<Organization> entities;
        long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId)) {
            entities = organizationRepository.findAllByMembers_Id(userLoggedInId, pageable);
        } else {
            entities = organizationRepository.findAll(pageable);
        }

        // Transform organizations
        DataPage<OrganizationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(OrganizationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Find all organizations for current user", description = "Find all organizations for current user", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all organizations for current user", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationModel.class))))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
    public Set<OrganizationModel> getAll(Principal principal) {

        // Get user organizations
        User user = userService.get(principal);
        Set<Organization> entities = organizationRepository.findAllByMembers_Id(user.getId());

        // Convert all entities to models
        Set<OrganizationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Find an organization by its ID", description = "Find an organization by its ID", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the organization", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = OrganizationModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrganizationModel getById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get organization by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // If user is not admin => organization where principal is member
        // Else => all organizations
        Organization entity;
        long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId)) {
            entity = organizationRepository.findByIdAndMembers_Id(id, userLoggedInId);
        } else {
            entity = organizationRepository.findById(id).orElse(null);
        }

        // Verify that entity exists
        if(entity == null) {
            LOGGER.error("Impossible to get organization by ID : organization not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        return OrganizationModel.fromEntity(entity);
    }

    @Operation(summary = "Get list of organizations by a list of IDs", description = "Find a list of organizations by a list of IDs", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the organizations", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<OrganizationModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<Organization> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<OrganizationModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            Organization entity = organizationRepository.findById(id).orElse(null);
            if(entity == null) {
                LOGGER.error("Impossible to get organization {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            if(!userLoggedInOrganizations.contains(entity) && userLoggedIn_isNotAdmin) {
                LOGGER.error("Impossible to get organization {} : principal {} is not in it", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(OrganizationModel.fromEntity(entity));
        }

        return models;
    }

    @Operation(summary = "Find all budgets for an organization", description = "Find all budgets for an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all budgets for an organization", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = BudgetModel.class)))),
            @ApiResponse(responseCode = "404", description = "Principal is not a member of organization", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/budgets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<BudgetModel> getBudgets(Principal principal, @PathVariable("id") Long organizationId) {

        // Verify that ID is correct
        if(organizationId <= 0) {
            LOGGER.error("Impossible to get budgets of organization : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is member of organization
        Long userLoggedInId = userService.get(principal).getId();
        Organization organization = organizationRepository.findByIdAndMembers_Id(organizationId, userLoggedInId);
        if(organization == null && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get budgets of organization {} : principal is not a member of organization", organizationId);
            throw new ForbiddenException();
        }

        // Get budget entities
        Set<Budget> entities = budgetRepository.findAllByOrganizationId(organizationId);

        // Convert all entities to models
        Set<BudgetModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(BudgetModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Create an organization", description = "Create an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Return the created organization", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = OrganizationModel.class))),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationModel create(Principal principal, @RequestBody OrganizationModel organizationModel) {

        // Verify that body is complete
        if(organizationModel == null || organizationModel.getName() == null) {
            LOGGER.error("Impossible to create organization : body is incomplete");
            throw new BadRequestException();
        }

        // Create organization
        Organization organization = (Organization) organizationModel;
        organization = organizationRepository.save(organization);

        // Create authorities
        for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
            organizationAuthorityRepository.save(new OrganizationAuthority(organization, authorityName));
        }

        // Grant principal with ROLE_OWNER
        User userLoggedIn = userService.get(principal);
        OrganizationAuthority organizationAuthority = organizationAuthorityRepository.findByOrganizationIdAndName(organization.getId(), OrganizationAuthorityName.ROLE_OWNER);
        userLoggedIn.getUserOrganizationAuthorities().add(organizationAuthority);
        userRepository.save(userLoggedIn);

        return OrganizationModel.fromEntity(organization);
    }

    @Operation(summary = "Update an organization", description = "Update an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is not complete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody OrganizationModel organizationModel) {

        // Verify that body is complete
        if(organizationModel == null || organizationModel.getName() == null || organizationModel.getId() <= 0) {
            LOGGER.error("Impossible to update organization : body is incomplete");
            throw new BadRequestException();
        }

        // Get corresponding entity
        Organization entity = organizationRepository.findById(organizationModel.getId()).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to update organization : organization {} not found", organizationModel.getId());
            throw new NotFoundException();
        }

        // Update entity
        entity.setName(organizationModel.getName());
        entity.setLogoUrl(organizationModel.getLogoUrl());
        organizationRepository.save(entity);
    }

    @Operation(summary = "Delete an organization by its ID", description = "Delete an organization by its ID", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization deleted", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(Principal principal, @PathVariable("id") long id) {

        // Fails if campaign ID is missing
        if(id <= 0) {
            LOGGER.error("Impossible to delete organization : ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Organization organization = organizationRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(organization == null) {
            LOGGER.error("Impossible to delete organization : donation {} not found", id);
            throw new NotFoundException();
        }

        // Verify that principal has correct privileges :
        // Principal is owner of the organization OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to delete organization : principal {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Delete donation
        organizationRepository.deleteById(id);
    }

    @Operation(summary = "Get members of an organization", description = "Get members of an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all members of organization", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/members", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<UserModel> getMembers(Principal principal, @PathVariable("id") long id, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get members of organizations : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get members of organizations : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get members of organizations : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Get and transform users
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("firstname").ascending().and(Sort.by("lastname").ascending()));
        Page<User> entities = userRepository.findAllByOrganizations_id(id, pageable);
        DataPage<UserModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(UserModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Add a member to an organization", description = "Add a member to an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or User not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/members/{userId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addMember(Principal principal, @PathVariable long id, @PathVariable long userId) {

        // Verify that IDs are corrects
        if(id <= 0 || userId <= 0) {
            LOGGER.error("Impossible to add member to an organization : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization and user exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if(organization == null || user == null) {
            LOGGER.error("Impossible to add member to an organization : organization {} or user {} doesnt exist", id, userId);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isManagerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to add member to an organization : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Add member to organization
        organization.getMembers().stream().filter(member -> member.getId().equals(user.getId()))
                .findAny()
                .ifPresentOrElse(
                        member -> {
                            LOGGER.error("Impossible to add member to an organization : User {} is already a member of organization {}", member.getId(), organization.getId());
                            throw new BadRequestException();
                        },
                        () -> {
                            organization.getMembers().add(user);
                            organizationRepository.save(organization);
                            LOGGER.info("User {} is now a member of organization {}", user.getId(), organization.getId());
                        }
                );
    }

    @Operation(summary = "Remove a member from an organization", description = "Remove a member from an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or User not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/members/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeMember(Principal principal, @PathVariable long id, @PathVariable long userId) {

        // Verify that IDs are corrects
        if(id <= 0 || userId <= 0) {
            LOGGER.error("Impossible to remove member from an organization : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization and user exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if(organization == null || user == null) {
            LOGGER.error("Impossible to remove member from an organization : organization {} or user {} doesnt exist", id, userId);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isManagerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to remove member from an organization : principal is not a manager of organization {}", id);
            throw new ForbiddenException();
        }

        // Remove privileges from user
        Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();
        user.getUserOrganizationAuthorities().stream().filter(organizationAuthority -> organizationAuthority.getOrganization().getId() == id).forEach(organizationAuthorities::add);
        user.getUserOrganizationAuthorities().removeAll(organizationAuthorities);
        userRepository.save(user);
        LOGGER.info("All Privileges for User {} on organization {} has been removed", user.getId(), organization.getId());

        // Remove member from organization
        organization.getMembers().remove(user);
        organizationRepository.save(organization);
        LOGGER.info("User {} has been removed from organization {}", user.getId(), organization.getId());
    }

    @Operation(summary = "Get paginated organization campaigns", description = "Get paginated organization campaigns", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return paginated campaigns", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Params are incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/campaigns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit", "filters"})
    public DataPage<CampaignModel> list(Principal principal, @PathVariable("id") Long id, @RequestParam("offset") int offset, @RequestParam("limit") int limit, @RequestParam("filters") List<String> filters) {

        // Verify that params are correct
        if(id <= 0 || offset < 0 || limit <= 0 || filters == null) {
            LOGGER.error("Impossible to get organization campaigns : params are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get organization campaigns : organization not found");
            throw new NotFoundException();
        }

        // Prepare filter
        Set<CampaignStatus> status = new LinkedHashSet<>();
        for(String filter : filters) {
            status.add(CampaignStatus.valueOf(filter));
        }
        if(status.isEmpty()) {
            status.addAll(List.of(CampaignStatus.values()));
        }

        // Get corresponding entities according to principal
        Long userLoggedInId = userService.get(principal).getId();
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("status").ascending().and(Sort.by("fundingDeadline").ascending()));
        Page<Campaign> entities;
        if(userService.isNotAdmin(userLoggedInId)) {
            entities = campaignRepository.findAllByOrganizations_IdAndStatusIn(id, status, pageable);
            //entities = campaignRepository.findAllByUserAndStatus(userLoggedInId, status, pageable);
        } else {
            entities = campaignRepository.findAllByStatusIn(status, pageable);
        }

        // Convert and return data
        DataPage<CampaignModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(Campaign.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get organization authorities", description = "Get organization authorities", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all organization authorities", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationAuthorityModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/authorities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<OrganizationAuthorityModel> getOrganizationAuthorities(Principal principal, @PathVariable("id") long id) {

        // Verify that IDs are corrects
        if(id <= 0) {
            LOGGER.error("Impossible to get organization authorities : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get organization authorities : organization not found");
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get organization authorities : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Get and transform organization authorities
        Set<OrganizationAuthority> entities = organizationAuthorityRepository.findAllByOrganizationId(organization.getId());
        Set<OrganizationAuthorityModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationAuthorityModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get paginated organization contents", description = "Get paginated organization contents", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return paginated organization contents", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/contents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public DataPage<ContentModel> getContents(Principal principal, @PathVariable("id") long id, @RequestParam(name = "offset") int offset, @RequestParam(name = "limit") int limit) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get contents of organizations : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get contents of organizations : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get contents of organizations : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Get and transform contents
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("name").ascending());
        Page<Content> entities = contentRepository.findAllByOrganizations_Id(pageable, id);
        DataPage<ContentModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(ContentModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get all organization contents", description = "Get all organization contents", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all organization contents", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = ContentModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/contents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<ContentModel> getAllContents(Principal principal, @PathVariable("id") long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get contents of organizations : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get contents of organizations : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get contents of organizations : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Get and transform contents
        Set<Content> entities = contentRepository.findAllByOrganizations_Id(id);
        Set<ContentModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(ContentModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Create a content for organization", description = "Create a content for organization", tags = { "Contents" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content is created", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/contents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addContent(Principal principal, @PathVariable("id") long id, @RequestBody ContentModel model) {

        // Verify that body is complete
        if(model == null || model.getName() == null) {
            LOGGER.error("Impossible to create content in organization : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to get contents of organizations : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to create content in organization : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Save content
        Content content = new Content();
        content.setName(model.getName());
        content.setValue(model.getValue());
        content = contentRepository.save(content);

        // Add content to organization
        organization.getContents().add(content);
        organizationRepository.save(organization);
    }

    @Operation(summary = "Remove a content from an organization", description = "Remove a content from an organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content removed", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or Content not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/contents", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeContent(Principal principal, @PathVariable long id, @RequestBody long contentId) {

        // Verify that IDs are corrects
        if(id <= 0 || contentId <= 0) {
            LOGGER.error("Impossible to remove content from an organization : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization and user exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        Content content = contentRepository.findById(contentId).orElse(null);
        if(organization == null || content == null) {
            LOGGER.error("Impossible to remove content from an organization : organization {} or content {} doesnt exist", id, contentId);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to remove content from an organization : principal is not a member of organization {}", id);
            throw new ForbiddenException();
        }

        // Remove content from organization
        organization.getContents().remove(content);
        organizationRepository.save(organization);
        if(content.getOrganizations().size() == 1) {
            contentRepository.deleteById(contentId);
        }
    }

    @Operation(summary = "Add Slack workspace to organization", description = "Add Slack workspace to organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slack workspace added", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/slack", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, params = {"code", "redirect_uri"})
    @ResponseStatus(HttpStatus.CREATED)
    public String slack(Principal principal, @PathVariable long id, @RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {

        // Verify that parameters are correct
        if(id <= 0 || code == null || code.isEmpty() || redirect_uri == null || redirect_uri.isEmpty()) {
            LOGGER.error("Impossible to add Slack workspace to organization : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to add Slack workspace to organization : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to add Slack workspace to organization : principal is not owner of organization {}", id);
            throw new ForbiddenException();
        }

        // Prepare Slack request
        String url = "https://slack.com/api/oauth.access?client_id=" + SLACK_CLIENT_ID + "&client_secret=" + SLACK_CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + redirect_uri;
        String body = "{\"code\":\"" + code + "\", \"redirect_uri\":\"" + redirect_uri + "\"}";
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuth())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;

        // Send Slack request and process response
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                SlackTeam slackTeam;
                if(organization.getSlackTeam() != null) {
                    slackTeam = organization.getSlackTeam();
                } else {
                    slackTeam = new SlackTeam();
                }
                JsonObject jsonBot = json.get("bot").getAsJsonObject();
                slackTeam.setAccessToken(json.get("access_token").getAsString());
                slackTeam.setTeamId(json.get("team_id").getAsString());
                slackTeam.setTeamName(json.get("team_name").getAsString());
                slackTeam.setBotAccessToken(jsonBot.get("bot_access_token").getAsString());
                slackTeam.setBotUserId(jsonBot.get("bot_user_id").getAsString());
                slackTeam.setOrganization(organization);
                slackTeamRepository.save(slackTeam);
            }
            return response.body();

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Impossible to add Slack workspace to organization");
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Operation(summary = "Sync Slack data with organization", description = "Sync Slack data with organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slack data synced", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/slack/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String slackSync(Principal principal, @PathVariable long id) throws InterruptedException {

        // Verify that parameters are correct
        if(id <= 0) {
            LOGGER.error("Impossible to sync Slack data with organization : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null) {
            LOGGER.error("Impossible to sync Slack data with organization : organization {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to sync Slack data with organization : principal is not owner of organization {}", id);
            throw new ForbiddenException();
        }

        // Get Slack users
        SlackTeam slackTeam = organization.getSlackTeam();
        List<SlackUser> slackUsers = slackClientService.listUsers(organization.getSlackTeam());

        // For each Slack user, retrieve its data
        User user;
        long delay;
        long tsAfterOpenIm = (new Timestamp(System.currentTimeMillis())).getTime();
        for(SlackUser slackUser : slackUsers) {

            // Sync with existing Slack user
            SlackUser slackUserEditted = slackUserRepository.findBySlackId(slackUser.getSlackId());
            if(slackUserEditted != null) {
                slackUserEditted.setName(slackUser.getName());
                slackUserEditted.setImage_192(slackUser.getImage_192());
                slackUserEditted.setEmail(slackUser.getEmail());
            } else {
                slackUserEditted = slackUser;
            }
            slackUserEditted.setSlackTeam(slackTeam);

            // Slack Open IM is Web API Tier 4 (100+ per minute) so wait 600ms
            delay = (new Timestamp(System.currentTimeMillis())).getTime() - tsAfterOpenIm;
            if(delay > 600) {
                delay = 600;
            }
            Thread.sleep(600 - delay);

            // Open IM with Slack user
            slackUserEditted.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUserEditted.getSlackId()));
            tsAfterOpenIm = (new Timestamp(System.currentTimeMillis())).getTime();

            // Sync with user
            user = userRepository.findByEmail(slackUser.getEmail());
            if(user == null) {
                user = UserGenerator.newUser(new User());
                user.setCreatedBy("Slack Sync");
                user.setFirstname(slackUserEditted.getName());
                user.setUsername(slackUserEditted.getEmail());
                user.setEmail(slackUserEditted.getEmail());
                user.setAvatarUrl(slackUserEditted.getImage_192());
                user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
            }
            user.setUpdatedBy("Slack Sync");
            user.setEnabled(!slackUser.getDeleted());

            // Save data
            final User userInDb = userRepository.save(user);
            slackUserEditted.setUser(userInDb);
            final SlackUser slackUserInDb = slackUserRepository.save(slackUserEditted);

            // Add Slack user to Slack Team users
            slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserInDb.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            slackTeamUser -> slackTeamUser = slackUserInDb,
                            () -> slackTeam.getSlackUsers().add(slackUserInDb));
            slackTeamRepository.save(slackTeam);

            // Add or remove user from organization according to enable parameter
            if(userInDb.getEnabled()) {
                organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                member -> member = userInDb,
                                () -> organization.getMembers().add(userInDb)
                        );
            } else {
                organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                        .findAny()
                        .ifPresent(member -> organization.getMembers().remove(member));
            }
            organizationRepository.save(organization);
        }
        return null;
    }

    @Operation(summary = "Disconnect Slack workspace from organization", description = "Disconnect Slack workspace from organization", tags = { "Organizations" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slack workspace removed", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Organization or User not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/organization/{id}/slack", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void slackDisconnect(Principal principal, @PathVariable long id) {

        // Verify that parameters are correct
        if(id <= 0) {
            LOGGER.error("Impossible disconnect Slack workspace from organization : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that organization and Slack Team exists
        Organization organization = organizationRepository.findById(id).orElse(null);
        if(organization == null ||organization.getSlackTeam() == null) {
            LOGGER.error("Impossible disconnect Slack workspace from organization : organization or Slack Team {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible disconnect Slack workspace from organization : principal is not owner of organization {}", id);
            throw new ForbiddenException();
        }

        // Delete Slack Team
        Long slackTeamId = organization.getSlackTeam().getId();
        slackUserRepository.deleteAllBySlackTeamId(slackTeamId);
        slackTeamRepository.deleteById(slackTeamId);
    }

    private static String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((OrganizationController.SLACK_CLIENT_ID + ":" + OrganizationController.SLACK_CLIENT_SECRET).getBytes());
    }

}
