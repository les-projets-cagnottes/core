package fr.lesprojetscagnottes.core.controller;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.model.DonationModel;
import fr.lesprojetscagnottes.core.model.OrganizationModel;
import fr.lesprojetscagnottes.core.model.ProjectModel;
import fr.lesprojetscagnottes.core.model.ProjectStatus;
import fr.lesprojetscagnottes.core.pagination.DataPage;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.scheduler.CampaignScheduler;
import fr.lesprojetscagnottes.core.service.SlackClientService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@Tag(name = "Projects", description = "The Projects API")
public class ProjectController {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private Gson gson;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private CampaignScheduler projectScheduler;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find a project by its ID", description = "Find a project by its ID", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the project", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectModel findById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            LOGGER.error("Impossible to get project by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in project organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<Organization> projectOrganizations = organizationRepository.findAllByProjects_Id(id);
        Set<Organization> principalOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.isNotAdmin(userLoggedInId) && projectOrganizations.stream().noneMatch(principalOrganizations::contains)) {
            LOGGER.error("Impossible to get project by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        Project entity = projectRepository.findById(id).orElse(null);
        if(entity == null) {
            LOGGER.error("Impossible to get project by ID : project not found");
            throw new NotFoundException();
        }

        // Transform and return organization
        return ProjectModel.fromEntity(entity);
    }

    @Operation(summary = "Get list of projects by a list of IDs", description = "Find a list of projects by a list of IDs", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the projects", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<ProjectModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<Organization> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<ProjectModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            Project project = projectRepository.findById(id).orElse(null);
            if(project == null) {
                LOGGER.error("Impossible to get project {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<Organization> projectOrganizations = organizationRepository.findAllByProjects_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, projectOrganizations) && userLoggedIn_isNotAdmin) {
                LOGGER.error("Impossible to get project {} : principal {} is not in its organizations", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(ProjectModel.fromEntity(project));
        }

        return models;
    }

    @Operation(summary = "Get paginated donations made on a project", description = "Get paginated donations made on a project", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding paginated donations", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<DonationModel> getDonations(Principal principal, @PathVariable("id") long projectId, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {

        // Fails if project ID is missing
        if(projectId <= 0) {
            LOGGER.error("Impossible to get donations by project ID : Project ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the project
        long userLoggedInId = userService.get(principal).getId();
        if(projectRepository.findByUserAndId(userLoggedInId, projectId).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get donations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(projectId).orElse(null);

        // Verify that any of references are not null
        if(project == null) {
            LOGGER.error("Impossible to get donations by project ID : project {} not found", projectId);
            throw new NotFoundException();
        }

        // Get and transform donations
        Page<Donation> entities = donationRepository.findByCampaign_idOrderByIdAsc(projectId, PageRequest.of(offset, limit, Sort.by("id")));
        DataPage<DonationModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(DonationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get project organizations", description = "Get project organizations", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding organizations", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrganizationModel.class)))),
            @ApiResponse(responseCode = "400", description = "Project ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<OrganizationModel> getOrganizations(Principal principal, @PathVariable("id") Long id) {

        // Fails if project ID is missing
        if(id <= 0) {
            LOGGER.error("Impossible to get organizations by project ID : Project ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the project
        long userLoggedInId = userService.get(principal).getId();
        if(projectRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to get organizations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(project == null) {
            LOGGER.error("Impossible to get organizations by project ID : project {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<Organization> entities = organizationRepository.findAllByProjects_Id(id);
        Set<OrganizationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Create a project", description = "Create a project", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectModel create(Principal principal, @RequestBody ProjectModel project) {

        // Fails if any of references are null
        if(project == null || project.getLeader() == null || project.getLeader().getId() < 0 ||
            project.getOrganizationsRef() == null || project.getOrganizationsRef().isEmpty()) {
            if(project != null ) {
                LOGGER.error("Impossible to create project \"{}\" : some references are missing", project.getTitle());
            } else {
                LOGGER.error("Impossible to create a null project");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        User leader = userRepository.findById(project.getLeader().getId()).orElse(null);
        Set<Organization> organizations = organizationRepository.findAllByIdIn(project.getOrganizationsRef());

        // Fails if any of references are null
        if(leader == null || organizations.isEmpty() ||
            organizations.size() != project.getOrganizationsRef().size()) {
            LOGGER.error("Impossible to create project \"{}\" : one or more reference(s) doesn't exist", project.getTitle());
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        User userLoggedIn = userService.get(principal);
        Set<Organization> organizationsMatch = organizationRepository.findAllByIdInAndMembers_Id(project.getOrganizationsRef(), userLoggedIn.getId());
        if(organizationsMatch.isEmpty()) {
            LOGGER.error("Impossible to create project \"{}\" : principal {} is not member of all organizations", project.getTitle(), userLoggedIn.getId());
            throw new ForbiddenException();
        }

        // Save project
        Project projectToSave = new Project();
        projectToSave.setTitle(project.getTitle());
        projectToSave.setStatus(ProjectStatus.A_DRAFT);
        projectToSave.setShortDescription(project.getShortDescription());
        projectToSave.setLongDescription(project.getLongDescription());
        projectToSave.setPeopleRequired(project.getPeopleRequired());
        projectToSave.setLeader(leader);
        projectToSave.getPeopleGivingTime().add(leader);
        final Project projectFinal = projectRepository.save(projectToSave);

        // Associate the project with organizations
        organizations.forEach(organization -> {
            organization.getProjects().add(projectFinal);
            projectFinal.getOrganizations().add(organization);
        });
        organizationRepository.saveAll(organizations);

        Map<String, Object> model = new HashMap<>();
        model.put("URL", WEB_URL);
        model.put("project", projectFinal);

        organizations.forEach(organization -> {
            if(organization.getSlackTeam() != null) {

                SlackTeam slackTeam = organization.getSlackTeam();

                organization.getMembers().stream()
                        .filter(member -> member.getId().equals(leader.getId()))
                        .findAny()
                        .ifPresentOrElse(member -> slackTeam.getSlackUsers().stream()
                                .filter(slackUser -> slackUser.getUser().getId().equals(leader.getId()))
                                .findAny()
                                .ifPresentOrElse(
                                        slackUser -> model.put("leader", "<@" + slackUser.getSlackId() + ">"),
                                        () -> model.put("leader", leader.getFullname())),() -> model.put("leader", leader.getFullname()));

                Context context = new Context();
                context.setVariables(model);
                String slackMessage = templateEngine.process("slack/fr/project-created", context);

                LOGGER.info("[create][" + project.getId() + "] Send Slack Message to " + slackTeam.getTeamId() + " / " + slackTeam.getPublicationChannelId() + " :\n" + slackMessage);

                slackClientService.inviteBotInConversation(slackTeam);
                slackClientService.postMessage(slackTeam, slackTeam.getPublicationChannelId(), slackMessage);
                LOGGER.info("[create][" + project.getId() + "] Slack Message Sent");
            }
        });

        project.setId(projectFinal.getId());
        return project;
    }

    @Operation(summary = "Update a project", description = "Update a project", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/project", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ProjectModel update(Principal principal, @RequestBody ProjectModel projectModel) {

        // Fails if any of references are null
        if(projectModel == null || projectModel.getId() <= 0 || projectModel.getLeader() == null || projectModel.getLeader().getId() < 0) {
            if(projectModel != null ) {
                LOGGER.error("Impossible to update project {} : some references are missing", projectModel.getId());
            } else {
                LOGGER.error("Impossible to update a null project");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        User leader = userRepository.findById(projectModel.getLeader().getId()).orElse(null);
        Project project = projectRepository.findById(projectModel.getId()).orElse(null);

        // Fails if any of references are null
        if(project == null || leader == null) {
            LOGGER.error("Impossible to update project {} : one or more reference(s) doesn't exist", projectModel.getId());
            throw new NotFoundException();
        }

        // Verify that principal has enough privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!leader.getId().equals(userLoggedInId) && userService.isNotAdmin(userLoggedInId)) {
            LOGGER.error("Impossible to update project {} : principal has not enough privileges", projectModel.getId());
        }

        // Save project
        project.setTitle(project.getTitle());
        project.setShortDescription(project.getShortDescription());
        project.setLongDescription(project.getLongDescription());
        project.setLeader(project.getLeader());
        project.setPeopleRequired(project.getPeopleRequired());

        return ProjectModel.fromEntity(projectRepository.save(project));
    }

    @Operation(summary = "Join a project", description = "Make principal join the project as a member", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project joined", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/join", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void join(Principal principal, @PathVariable("id") Long id) {

        // Fails if any of references are null
        if(id < 0) {
            LOGGER.error("Impossible to join project {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(project == null) {
            LOGGER.error("Impossible to join project {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        User userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        project.setOrganizations(organizationRepository.findAllByProjects_Id(id));
        Set<Long> organizationsRef = new LinkedHashSet<>();
        project.getOrganizations().forEach(organization -> organizationsRef.add(organization.getId()));
        if(organizationRepository.findAllByIdInAndMembers_Id(organizationsRef, userLoggedInId).isEmpty()) {
            LOGGER.error("Impossible to join project {} : principal {} is not member of all organizations", id, userLoggedInId);
            throw new ForbiddenException();
        }

        // Add or remove member
        project.setPeopleGivingTime(userRepository.findAllByProjects_Id(id));
        project.getPeopleGivingTime()
                .stream()
                .filter(member -> userLoggedInId.equals(member.getId()))
                .findAny()
                .ifPresentOrElse(
                        user -> project.getPeopleGivingTime().remove(user),
                        () -> project.getPeopleGivingTime().add(userLoggedIn));
        projectRepository.save(project);
    }


}
