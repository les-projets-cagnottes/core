package fr.lesprojetscagnottes.core.controller;

import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.entity.model.DonationModel;
import fr.lesprojetscagnottes.core.entity.model.OrganizationAuthorityModel;
import fr.lesprojetscagnottes.core.entity.model.OrganizationModel;
import fr.lesprojetscagnottes.core.entity.model.UserModel;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.pagination.DataPage;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@RequestMapping("/api")
@Tag(name = "Users", description = "The Users API")
@RestController
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get paginated users", description = "Get paginated users", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return paginated users", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public DataPage<UserModel> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {

        // Verify that ID is correct
        if(offset <= 0 || limit <= 0) {
            LOGGER.error("Impossible to get contents of organizations : ID is incorrect");
            throw new BadRequestException();
        }

        // Get and transform contents
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("name").ascending());
        Page<User> entities = userRepository.findAll(pageable);
        DataPage<UserModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(UserModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get user by its ID", description = "Find a user by its ID", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the user", content = @Content(schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserModel getById(Principal principal, @PathVariable("id") long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get user by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that current user is the same as requested
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(id) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get user by ID : principal is not the requested user");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        User entity = userRepository.findById(id).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to get user by ID : user not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        UserModel model = UserModel.fromEntity(entity);
        model.emptyPassword();
        return model;
    }

    @Operation(summary = "Get user by its email", description = "Find a user by its email", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the user", content = @Content(schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "400", description = "Email is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"email"})
    public UserModel getByEmail(Principal principal, @RequestParam("email") String email) {

        // Verify that ID is correct
        if(email == null || email.isEmpty()) {
            LOGGER.error("Impossible to get user by email : email is incorrect");
            throw new BadRequestException();
        }

        // Verify that current user is the same as requested
        User userLoggedIn = userService.get(principal);
        if(!userLoggedIn.getEmail().equals(email) && userService.isNotAdmin(userLoggedIn.getId())) {
            LOGGER.error("Impossible to get user by email : principal is not the requested user");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        User entity = userRepository.findByEmail(email);
        if(entity == null) {
            LOGGER.error("Impossible to get user by email : user not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        UserModel model = UserModel.fromEntity(entity);
        model.emptyPassword();
        return model;
    }

    @Operation(summary = "Get user organizations", description = "Get user organizations", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding organizations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<OrganizationModel> getOrganizations(Principal principal, @PathVariable("id") long id) {

        // Fails if project ID is missing
        if(id <= 0) {
            LOGGER.error("Impossible to get organizations by user ID : User ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the user OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != id && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get organizations by user ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        User user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            LOGGER.error("Impossible to get organizations by user ID : user {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<Organization> entities = organizationRepository.findAllByMembers_Id(id);
        Set<OrganizationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get donations made by a user", description = "Get donations made by a user", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<DonationModel> getDonations(Principal principal, @PathVariable("id") long contributorId) {

        // Fails if project ID is missing
        if(contributorId <= 0) {
            LOGGER.error("Impossible to get donations by contributor ID : Contributor ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != contributorId && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by contributor ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        User user = userRepository.findById(contributorId).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            LOGGER.error("Impossible to get donations by contributor ID : user {} not found", contributorId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Set<Donation> entities = donationRepository.findAllByContributorIdOrderByBudgetIdAsc(contributorId);
        Set<DonationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(DonationModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Get donations made by a user imputed on a budget", description = "Get donations made by a user imputed on a budget", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId"})
    public Set<DonationModel> getDonationsByBudgetId(Principal principal, @PathVariable("id") long contributorId, @RequestParam("budgetId") long budgetId) {

        // Fails if project ID is missing
        if(contributorId <= 0 || budgetId <= 0) {
            LOGGER.error("Impossible to get donations by contributor ID and budget ID : Contributor ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != contributorId && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by contributor ID and budget ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Budget budget = budgetRepository.findById(budgetId).orElse(null);
        User user = userRepository.findById(contributorId).orElse(null);

        // Verify that any of references are not null
        if(user == null || budget == null) {
            LOGGER.error("Impossible to get donations by contributor ID and budget ID : user {} or budget {} not found", contributorId, budget);
            throw new NotFoundException();
        }

        // Get and transform donations
        Set<Donation> entities = donationRepository.findAllByContributorIdAndBudgetId(contributorId, budgetId);
        Set<DonationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(DonationModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Create a user", description = "Create a user", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody UserModel userModel) {

        // Verify that body is complete
        if(userModel == null ||
                userModel.getEmail() == null || userModel.getEmail().isEmpty() ||
                userModel.getPassword() == null || userModel.getPassword().isEmpty()) {
            LOGGER.error("Impossible to update user : body is incomplete");
            throw new BadRequestException();
        }

        User user = UserGenerator.newUser((User) userModel);
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userRepository.save(user);
    }

    @Operation(summary = "Update a user", description = "Update a user", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(Principal principal, @RequestBody UserModel userModel) {

        // Verify that body is complete
        if(userModel == null ||
                userModel.getId() <= 0 ||
                userModel.getEmail() == null || userModel.getEmail().isEmpty()) {
            LOGGER.error("Impossible to update user : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(userModel.getId()) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to update user : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that user exists
        User user = userRepository.findById(userModel.getId()).orElse(null);
        if(user == null) {
            LOGGER.error("Impossible to update user : user {} not found", userModel.getId());
            throw new NotFoundException();
        }

        // Update and save user
        if(user.getPassword() != null && !userModel.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(userModel.getPassword(), BCrypt.gensalt()));
            user.setLastPasswordResetDate(new Date());
        }
        user.setUsername(userModel.getUsername());
        user.setEmail(userModel.getEmail());
        user.setFirstname(userModel.getFirstname());
        user.setLastname(userModel.getLastname());
        user.setAvatarUrl(userModel.getAvatarUrl());
        user.setEnabled(userModel.getEnabled());
        userRepository.save(user);
    }

    @Operation(summary = "Grant a user with an organization authority", description = "Grant a user with an organization authority", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User granted", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Authority not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/orgauthorities", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void grant(Principal principal, @PathVariable long id, @RequestBody OrganizationAuthorityModel organizationAuthority) {

        // All prerequisites are presents
        if(id <= 0 || organizationAuthority == null || organizationAuthority.getId() <= 0) {
            LOGGER.error("Impossible to grant user {} with organization authority {} : some parameters are missing", id, organizationAuthority);
            throw new BadRequestException();
        }

        // Verify user and organization authority exists in DB
        final User userInDb = userRepository.findById(id).orElse(null);
        OrganizationAuthority organizationAuthorityInDb = organizationAuthorityRepository.findById(organizationAuthority.getId()).orElse(null);
        if(userInDb == null || organizationAuthorityInDb == null) {
            LOGGER.error("Impossible to grant user {} with organization authority {} : cannot find user or authority in DB", id, organizationAuthority.getId());
            throw new NotFoundException();
        }

        // Test that user logged in has correct rights
        User userLoggedIn = userService.get(principal);
        if(organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organizationAuthorityInDb.getOrganization().getId(), userLoggedIn.getId(), OrganizationAuthorityName.ROLE_OWNER) == null &&
                authorityRepository.findByNameAndUsers_Id(AuthorityName.ROLE_ADMIN, userLoggedIn.getId()) == null) {
            LOGGER.error("Impossible to grant user {} with organization authority {} : principal has not enough privileges", id, organizationAuthority.getId());
            throw new ForbiddenException();
        }

        // Verify that user we want to grant is member of target organization
        Organization organization = organizationRepository.findByIdAndMembers_Id(organizationAuthorityInDb.getOrganization().getId(), userInDb.getId()).orElse(null);
        LOGGER.debug(String.valueOf(organization));
        if(organizationRepository.findByIdAndMembers_Id(organizationAuthorityInDb.getOrganization().getId(), userInDb.getId()).isEmpty()) {
            LOGGER.error("Impossible to grant user {} with organization authority {} : user is not member of target organization", id, organizationAuthority.getId());
            throw new BadRequestException();
        }

        // Grant or remove organization authority
        userInDb.getUserOrganizationAuthorities().stream().filter(authority -> authority.getId().equals(organizationAuthorityInDb.getId()))
                .findAny()
                .ifPresentOrElse(
                        authority -> {
                            LOGGER.debug("Remove organization authority {} from user {}", authority.getId(), userInDb.getId());
                            userInDb.getUserOrganizationAuthorities().remove(authority);
                        },
                        () -> {
                            LOGGER.debug("Add organization authority {} to user {}", organizationAuthorityInDb.getId(), userInDb.getId());
                            userInDb.getUserOrganizationAuthorities().add(organizationAuthorityInDb);
                        }
                );
        userRepository.save(userInDb);
    }

    @Operation(summary = "Delete a user", description = "Delete a user by its ID", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") long id) {

        // Fails if project ID is missing
        if(id <= 0) {
            LOGGER.error("Impossible to delete user : ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        User user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            LOGGER.error("Impossible to delete user : user {} not found", id);
            throw new NotFoundException();
        }

        // Remove user from organizations
        Set<Organization> organizations = organizationRepository.findAllByMembers_Id(id);
        organizations.forEach(organization -> {
            organization.getMembers().remove(user);
            organizationRepository.save(organization);
        });

        // Remove user from projects
        Set<Campaign> campaigns = campaignRepository.findAllByPeopleGivingTime_Id(id);
        campaigns.forEach(project -> {
            project.getPeopleGivingTime().remove(user);
            campaignRepository.save(project);
        });

        // Delete user
        userRepository.deleteById(id);
    }

}
