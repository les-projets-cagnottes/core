package fr.lesprojetscagnottes.core.project.controller;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.news.model.NewsModel;
import fr.lesprojetscagnottes.core.news.repository.NewsRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationModel;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
import fr.lesprojetscagnottes.core.project.model.ProjectStatus;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.user.UserService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Projects", description = "The Projects API")
public class ProjectController {

    @Autowired
    private Gson gson;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NewsRepository newsRepository;

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
            log.error("Impossible to get project by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in project organizations
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> projectOrganizations = organizationRepository.findAllByProjects_Id(id);
        Set<OrganizationEntity> principalOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        if(userService.isNotAdmin(userLoggedInId) && projectOrganizations.stream().noneMatch(principalOrganizations::contains)) {
            log.error("Impossible to get project by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Verify that entity exists
        ProjectEntity entity = projectRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to get project by ID : project not found");
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
        Set<OrganizationEntity> userLoggedInOrganizations = organizationRepository.findAllByMembers_Id(userLoggedInId);
        Set<ProjectModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            ProjectEntity project = projectRepository.findById(id).orElse(null);
            if(project == null) {
                log.error("Impossible to get project {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> projectOrganizations = organizationRepository.findAllByProjects_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, projectOrganizations) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get project {} : principal {} is not in its organizations", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(ProjectModel.fromEntity(project));
        }

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
            log.error("Impossible to get organizations by project ID : Project ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that principal is in one organization of the project
        long userLoggedInId = userService.get(principal).getId();
        if(projectRepository.findByUserAndId(userLoggedInId, id).isEmpty() && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get organizations by project ID : user {} is not member of concerned organizations", userLoggedInId);
            throw new ForbiddenException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(id).orElse(null);

        // Verify that any of references are not null
        if(project == null) {
            log.error("Impossible to get organizations by project ID : project {} not found", id);
            throw new NotFoundException();
        }

        // Get and transform entities
        Set<OrganizationEntity> entities = organizationRepository.findAllByProjects_Id(id);
        Set<OrganizationModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(OrganizationModel.fromEntity(entity)));
        return models;
    }

    @Operation(summary = "Get paginated news", description = "Get paginated news", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding news", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Budget ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/news", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public DataPage<NewsModel> listNews(Principal principal, @PathVariable("id") Long id, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {

        // Verify that IDs are corrects
        if(id <= 0) {
            log.error("Impossible to get news : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        ProjectEntity project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            log.error("Impossible to get news : project not found");
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        boolean hasNoPrivilege = userService.isNotAdmin(userLoggedInId);
        Set<OrganizationEntity> organizations = project.getOrganizations();
        if(hasNoPrivilege) {
            for(OrganizationEntity org : organizations) {
                hasNoPrivilege &= !userService.isMemberOfOrganization(userLoggedInId, org.getId());
            }
        }
        if(hasNoPrivilege) {
            log.error("Impossible to get news : principal is not a member of any of the project organizations");
            throw new ForbiddenException();
        }

        // Get and transform donations
        Page<NewsEntity> entities = newsRepository.findAllByProjectId(id, PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
        DataPage<NewsModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(NewsModel.fromEntity(entity)));
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
                log.error("Impossible to create project \"{}\" : some references are missing", project.getTitle());
            } else {
                log.error("Impossible to create a null project");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        UserEntity leader = userRepository.findById(project.getLeader().getId()).orElse(null);
        Set<OrganizationEntity> organizations = organizationRepository.findAllByIdIn(project.getOrganizationsRef());

        // Fails if any of references are null
        if(leader == null || organizations.isEmpty() ||
            organizations.size() != project.getOrganizationsRef().size()) {
            log.error("Impossible to create project \"{}\" : one or more reference(s) doesn't exist", project.getTitle());
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        Set<OrganizationEntity> organizationsMatch = organizationRepository.findAllByIdInAndMembers_Id(project.getOrganizationsRef(), userLoggedIn.getId());
        if(organizationsMatch.isEmpty()) {
            log.error("Impossible to create project \"{}\" : principal {} is not member of all organizations", project.getTitle(), userLoggedIn.getId());
            throw new ForbiddenException();
        }

        // Save project
        ProjectEntity projectToSave = new ProjectEntity();
        projectToSave.setTitle(project.getTitle());
        projectToSave.setStatus(ProjectStatus.DRAFT);
        projectToSave.setShortDescription(project.getShortDescription());
        projectToSave.setLongDescription(project.getLongDescription());
        projectToSave.setPeopleRequired(project.getPeopleRequired());
        projectToSave.setWorkspace(project.getWorkspace());
        projectToSave.setLeader(leader);
        projectToSave.getPeopleGivingTime().add(leader);
        final ProjectEntity projectFinal = projectRepository.save(projectToSave);

        organizations.forEach(organization -> {

            // Associate the project with organizations
            organization.getProjects().add(projectFinal);
            projectFinal.getOrganizations().add(organization);

        });
        organizationRepository.saveAll(organizations);

        return ProjectModel.fromEntity(projectFinal);
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
                log.error("Impossible to update project {} : some references are missing", projectModel.getId());
            } else {
                log.error("Impossible to update a null project");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        UserEntity leader = userRepository.findById(projectModel.getLeader().getId()).orElse(null);
        ProjectEntity project = projectRepository.findById(projectModel.getId()).orElse(null);

        // Fails if any of references are null
        if(project == null || leader == null) {
            log.error("Impossible to update project {} : one or more reference(s) doesn't exist", projectModel.getId());
            throw new NotFoundException();
        }

        // Verify that principal has enough privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(!leader.getId().equals(userLoggedInId) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to update project {} : principal has not enough privileges", projectModel.getId());
        }

        // Save project
        project.setTitle(projectModel.getTitle());
        project.setShortDescription(projectModel.getShortDescription());
        project.setLongDescription(projectModel.getLongDescription());
        project.setLeader(projectModel.getLeader());
        project.setPeopleRequired(projectModel.getPeopleRequired());

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
            log.error("Impossible to join project {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(project == null) {
            log.error("Impossible to join project {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        project.setOrganizations(organizationRepository.findAllByProjects_Id(id));
        Set<Long> organizationsRef = new LinkedHashSet<>();
        project.getOrganizations().forEach(organization -> organizationsRef.add(organization.getId()));
        if(organizationRepository.findAllByIdInAndMembers_Id(organizationsRef, userLoggedInId).isEmpty()) {
            log.error("Impossible to join project {} : principal {} is not member of all organizations", id, userLoggedInId);
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

    @Operation(summary = "Publish a project", description = "Update the project with the 'in progress' state", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project published", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/publish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void publish(Principal principal, @PathVariable("id") Long id) {

        // Fails if any of references are null
        if(id < 0) {
            log.error("Impossible to publish project {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(project == null) {
            log.error("Impossible to publish project {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal has enough privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to publish project {} : principal has not enough privileges", project.getId());
        }

        // Add or remove member
        if(project.getStatus().equals(ProjectStatus.DRAFT)) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(project);
        }
    }

}
