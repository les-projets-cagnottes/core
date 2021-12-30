package fr.lesprojetscagnottes.core.user.controller;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.account.repository.AccountRepository;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.model.OrganizationAuthorityModel;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.campaign.model.CampaignModel;
import fr.lesprojetscagnottes.core.campaign.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.model.OrganizationModel;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import fr.lesprojetscagnottes.core.slack.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.slack.repository.SlackUserRepository;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.model.UserModel;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import fr.lesprojetscagnottes.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Users", description = "The Users API")
@RestController
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get paginated users", description = "Get paginated users", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return paginated users", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Params are incorrects", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public DataPage<UserModel> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {

        // Verify that params are correct
        if(offset < 0 || limit <= 0) {
            log.error("Impossible to get contents of organizations : ID is incorrect");
            throw new BadRequestException();
        }

        // Get and transform contents
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("firstname").ascending());
        Page<UserEntity> entities = userRepository.findAll(pageable);
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
            log.error("Impossible to get user by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that current user and user requested shares an organization
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<OrganizationEntity> userOrganizations = organizationRepository.findAllByMembers_Id(id);
        if(userService.hasNoACommonOrganization(userLoggedInOrganizations, userOrganizations) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get user {} : principal {} and him does not share an organization", id, userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that entity exists
        UserEntity entity = userRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to get user by ID : user not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        UserModel model = UserModel.fromEntity(entity);
        model.emptyPassword();
        return model;
    }

    @Operation(summary = "Get list of user by a list of IDs", description = "Find a list of user by a list of IDs", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the users", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<UserModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<OrganizationEntity> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<UserModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            UserEntity user = userRepository.findById(id).orElse(null);
            if(user == null) {
                log.error("Impossible to get user {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> userOrganizations = organizationRepository.findAllByMembers_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, userOrganizations) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get user {} : principal {} and him does not share an organization", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(UserModel.fromEntity(user));
        }

        return models;
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
            log.error("Impossible to get user by email : email is incorrect");
            throw new BadRequestException();
        }

        // Verify that current user is the same as requested
        UserEntity userLoggedIn = userService.get(principal);
        if(!userLoggedIn.getEmail().equals(email) && userService.isNotAdmin(userLoggedIn.getId())) {
            log.error("Impossible to get user by email : principal is not the requested user");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        UserEntity entity = userRepository.findByEmail(email);
        if(entity == null) {
            log.error("Impossible to get user by email : user not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        UserModel model = UserModel.fromEntity(entity);
        model.emptyPassword();
        return model;
    }

    @Operation(summary = "Get user accounts", description = "Get user accounts", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding accounts", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/accounts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AccountModel> getAccounts(Principal principal, @PathVariable("id") Long id) {

        // Fails if user ID is missing
        if(id <= 0) {
            log.error("Impossible to get accounts by user ID : User ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the user OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(id) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get accounts by user ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get accounts by user ID : user {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<AccountModel> models = new LinkedHashSet<>();
        Set<AccountEntity> entities = accountRepository.findAllByOwnerId(id);
        entities.forEach(entity -> models.add(AccountModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Get user accounts matching budget IDs", description = "Get user accounts matching budget IDs", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding accounts", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/accounts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetIds"})
    public Set<AccountModel> getAccountsByBudgetIds(Principal principal, @PathVariable("id") Long id, @RequestParam("budgetIds") Set<Long> budgetIds) {

        // Fails if user ID is missing
        if(id <= 0 || budgetIds.size() <= 0) {
            log.error("Impossible to get accounts by user ID & budget IDs : params are incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the user OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(id) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get accounts by user ID & budget IDs : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get accounts by user ID & budget IDs : user {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<AccountModel> models = new LinkedHashSet<>();
        Set<AccountEntity> entities = accountRepository.findAllByOwnerIdAndBudgetIdIn(id, budgetIds);
        entities.forEach(entity -> models.add(AccountModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Get user campaigns", description = "Get user projects", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding projects", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/projects", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<ProjectModel> getProjects(Principal principal, @PathVariable("id") Long id) {

        // Fails if user ID is missing
        if(id <= 0) {
            log.error("Impossible to get projects by user ID : User ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the user OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != id && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get projects by user ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get projects by user ID : user {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<ProjectModel> models = new LinkedHashSet<>();
        Set<ProjectEntity> entities = projectRepository.findAllByLeaderId(id);
        entities.addAll(projectRepository.findAllByPeopleGivingTime_Id(id));
        entities.forEach(entity -> models.add(ProjectModel.fromEntity(entity)));

        return models;
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

        // Fails if user ID is missing
        if(id <= 0) {
            log.error("Impossible to get organizations by user ID : User ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the user OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        boolean isNotAdmin = userService.isNotAdmin(userLoggedInId);
        if(userLoggedInId != id && isNotAdmin) {
            log.error("Impossible to get organizations by user ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get organizations by user ID : user {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<OrganizationModel> models = new LinkedHashSet<>();
        if(isNotAdmin) {
            Set<OrganizationEntity> entities = organizationRepository.findAllByMembers_Id(id);
            entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        } else {
            List<OrganizationEntity> entities = organizationRepository.findAll();
            entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        }
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

        // Fails if campaign ID is missing
        if(contributorId <= 0) {
            log.error("Impossible to get donations by contributor ID : Contributor ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != contributorId && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get donations by contributor ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(contributorId).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get donations by contributor ID : user {} not found", contributorId);
            throw new NotFoundException();
        }

        // Find all user accounts
        Set<AccountEntity> accounts = accountRepository.findAllByOwnerId(contributorId);
        Set<Long> accountIds = new LinkedHashSet<>();
        accounts.forEach(account -> accountIds.add(account.getId()));

        // Get and transform donations
        Set<Donation> entities = donationRepository.findAllByAccountIdInOrderByCreatedAtAsc(accountIds);
        Set<DonationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(DonationModel.fromEntity(entity)));

        return models;
    }

    @Operation(summary = "Get donations made by a user matching account IDs", description = "Get donations made by a user matching account IDs", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding donations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"accountIds"})
    public Set<DonationModel> getDonationsByAccountIds(Principal principal, @PathVariable("id") long contributorId, @RequestParam("accountIds") Set<Long> accountIds) {

        // Fails if campaign ID is missing
        if(contributorId <= 0 || accountIds.size() <= 0) {
            log.error("Impossible to get donations by contributor ID : params are incorrect");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges
        long userLoggedInId = userService.get(principal).getId();
        if(userLoggedInId != contributorId && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get donations by contributor ID : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        UserEntity user = userRepository.findById(contributorId).orElse(null);

        // Verify that any of references are not null
        if(user == null) {
            log.error("Impossible to get donations by contributor ID : user {} not found", contributorId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Set<Donation> entities = donationRepository.findAllByAccountIdInOrderByCreatedAtAsc(accountIds);
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
            log.error("Impossible to update user : body is incomplete");
            throw new BadRequestException();
        }

        UserEntity user = new UserEntity();
        UserGenerator.newUser(user);
        user.setUsername(userModel.getUsername());
        user.setEmail(userModel.getEmail());
        user.setFirstname(userModel.getFirstname());
        user.setLastname(userModel.getLastname());
        user.setAvatarUrl(userModel.getAvatarUrl());
        user.setEnabled(userModel.getEnabled());
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
    }

    @Operation(summary = "Update a user", description = "Update a user", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Body is incomplete", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(Principal principal, @RequestBody UserModel userModel) {

        // Verify that body is complete
        if(userModel == null ||
                userModel.getId() <= 0 ||
                userModel.getEmail() == null || userModel.getEmail().isEmpty()) {
            log.error("Impossible to update user : body is incomplete");
            throw new BadRequestException();
        }

        // Verify that principal has correct privileges :
        // Principal is the contributor OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(userModel.getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to update user : user {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        // Verify that user exists
        UserEntity user = userRepository.findById(userModel.getId()).orElse(null);
        if(user == null) {
            log.error("Impossible to update user : user {} not found", userModel.getId());
            throw new NotFoundException();
        }

        // Update and save user
        if(userModel.getPassword() != null && !userModel.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userModel.getPassword()));
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

    @Operation(summary = "Find all organization authorities", description = "Find all organization authorities", tags = { "Users" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return all organization authorities", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationAuthorityModel.class)))),
            @ApiResponse(responseCode = "400", description = "User ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/user/{id}/orgauthorities", method = RequestMethod.GET)
    public Set<OrganizationAuthorityModel> getUserOrganizationAuthorities(Principal principal, @PathVariable Long id) {

        // Fails if ID is incorrect
        if(id <= 0) {
            log.error("Impossible to get organization authorities for user {} : ID is incorrect", id);
            throw new BadRequestException();
        }

        // Verify that current user is the same as requested
        Long userLoggedInId = userService.get(principal).getId();
        if(!userLoggedInId.equals(id) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get organization authorities for user {} : principal is not the requested user", id);
            throw new ForbiddenException();
        }

        // Get user organizations
        Set<OrganizationAuthorityEntity> entities = organizationAuthorityRepository.findAllByUsers_Id(id);

        // Convert all entities to models
        Set<OrganizationAuthorityModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationAuthorityModel.fromEntity(entity)));

        return models;
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
            log.error("Impossible to grant user {} with organization authority {} : some parameters are missing", id, organizationAuthority);
            throw new BadRequestException();
        }

        // Verify user and organization authority exists in DB
        final UserEntity userInDb = userRepository.findById(id).orElse(null);
        OrganizationAuthorityEntity organizationAuthorityInDb = organizationAuthorityRepository.findById(organizationAuthority.getId()).orElse(null);
        if(userInDb == null || organizationAuthorityInDb == null) {
            log.error("Impossible to grant user {} with organization authority {} : cannot find user or authority in DB", id, organizationAuthority.getId());
            throw new NotFoundException();
        }

        // Test that user logged in has correct rights
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId) && !userService.isOwnerOfOrganization(userLoggedInId, organizationAuthorityInDb.getOrganization().getId())) {
            log.error("Impossible to grant user {} with organization authority {} : principal has not enough privileges", id, organizationAuthority.getId());
            throw new ForbiddenException();
        }

        // Verify that user we want to grant is member of target organization
        if(!userService.isMemberOfOrganization(userInDb.getId(), organizationAuthorityInDb.getOrganization().getId())) {
            log.error("Impossible to grant user {} with organization authority {} : user is not member of target organization", id, organizationAuthority.getId());
            throw new BadRequestException();
        }

        // Grant or remove organization authority
        userInDb.getUserOrganizationAuthorities().stream().filter(authority -> authority.getId().equals(organizationAuthorityInDb.getId()))
                .findAny()
                .ifPresentOrElse(
                        authority -> {
                            log.debug("Remove organization authority {} from user {}", authority.getId(), userInDb.getId());
                            userInDb.getUserOrganizationAuthorities().remove(authority);
                        },
                        () -> {
                            log.debug("Add organization authority {} to user {}", organizationAuthorityInDb.getId(), userInDb.getId());
                            userInDb.getUserOrganizationAuthorities().add(organizationAuthorityInDb);
                        }
                );
        userRepository.save(userInDb);
    }

}
