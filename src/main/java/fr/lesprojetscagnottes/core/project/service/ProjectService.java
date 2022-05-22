package fr.lesprojetscagnottes.core.project.service;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.notification.model.NotificationName;
import fr.lesprojetscagnottes.core.notification.service.NotificationService;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.service.OrganizationService;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
import fr.lesprojetscagnottes.core.project.model.ProjectStatus;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.model.UserModel;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ProjectService {

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectRepository projectRepository;

    public ProjectEntity findById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public ProjectModel findById(Principal principal, Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            log.error("Impossible to get project by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        ProjectEntity entity = findById(id);
        if(entity == null) {
            log.error("Impossible to get project by ID : project not found");
            throw new NotFoundException();
        }

        // Verify that principal is in project organizations
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId) && !userService.isMemberOfOrganization(userLoggedInId, entity.getOrganization().getId())) {
            log.error("Impossible to get project by ID : principal has not enough privileges");
            throw new ForbiddenException();
        }

        // Transform and return organization
        return ProjectModel.fromEntity(entity);
    }

    public Set<ProjectModel> getByIds(Principal principal, Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<OrganizationEntity> userLoggedInOrganizations = organizationService.findAllByMembers_Id(userLoggedInId);
        Set<ProjectModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            ProjectEntity project = projectRepository.findById(id).orElse(null);
            if(project == null) {
                log.error("Impossible to get project {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            Set<OrganizationEntity> projectOrganizations = organizationService.findAllByProjects_Id(id);
            if(userService.hasNoACommonOrganization(userLoggedInOrganizations, projectOrganizations) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get project {} : principal {} is not in its organizations", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(ProjectModel.fromEntity(project));
        }

        return models;
    }

    public Set<UserModel> listTeammates(Principal principal, Long id) {

        // Verify that IDs are corrects
        if(id <= 0) {
            log.error("Impossible to get teammates : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        ProjectEntity project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            log.error("Impossible to get teammates : project not found");
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId) && !userService.isMemberOfOrganization(userLoggedInId, project.getOrganization().getId())) {
            log.error("Impossible to get teammates : principal is not a member of any of the project organizations");
            throw new ForbiddenException();
        }

        // Get users
        Set<UserEntity> entities = userService.findAllByProjects_Id(id);
        Set<UserModel> models = new LinkedHashSet<>();
        entities.forEach(entity -> models.add(UserModel.fromEntity(entity)));
        return models;
    }

    public ProjectModel create(Principal principal, ProjectModel project) {

        // Fails if any of references are null
        if(project == null || project.getLeader() == null || project.getLeader().getId() <= 0 ||
                project.getOrganization() == null || project.getOrganization().getId() <= 0) {
            if(project != null ) {
                log.error("Impossible to create project \"{}\" : some references are missing", project.getTitle());
            } else {
                log.error("Impossible to create a null project");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        UserEntity leader = userService.findById(project.getLeader().getId());
        OrganizationEntity organization = organizationService.findById(project.getOrganization().getId());

        // Fails if any of references are null
        if(leader == null || organization == null) {
            log.error("Impossible to create project \"{}\" : one or more reference(s) doesn't exist", project.getTitle());
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId) && !userService.isMemberOfOrganization(userLoggedInId, organization.getId())) {
            log.error("Impossible to create project \"{}\" : principal {} is not member of organization", project.getTitle(), userLoggedInId);
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
        projectToSave.setOrganization(organization);
        projectToSave.getPeopleGivingTime().add(leader);

        return ProjectModel.fromEntity(projectRepository.save(projectToSave));
    }

    public ProjectModel update(Principal principal, ProjectModel projectModel) {

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
        UserEntity leader = userService.findById(projectModel.getLeader().getId());
        OrganizationEntity organization = organizationService.findById(projectModel.getOrganization().getId());
        ProjectEntity project = projectRepository.findById(projectModel.getId()).orElse(null);

        // Fails if any of references are null
        if(project == null || leader == null || organization == null) {
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
        project.setPeopleRequired(projectModel.getPeopleRequired());
        project.setWorkspace(projectModel.getWorkspace());
        project.setLeader(projectModel.getLeader());
        project.setOrganization(projectModel.getOrganization());

        return ProjectModel.fromEntity(projectRepository.save(project));
    }

    public void join(Principal principal, Long id) {

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
        if(!userService.isMemberOfOrganization(userLoggedInId, project.getOrganization().getId())) {
            log.error("Impossible to join project {} : principal {} is not member of organization", id, userLoggedInId);
            throw new ForbiddenException();
        }

        // Fails if project is finished
        if(project.getStatus().equals(ProjectStatus.FINISHED)) {
            log.error("Impossible to join project {} : status is finished", id);
        }

        // Add or remove member
        project.setPeopleGivingTime(userService.findAllByProjects_Id(id));
        project.getPeopleGivingTime()
                .stream()
                .filter(member -> userLoggedInId.equals(member.getId()))
                .findAny()
                .ifPresentOrElse(
                        user -> project.getPeopleGivingTime().remove(user),
                        () -> project.getPeopleGivingTime().add(userLoggedIn));
        projectRepository.save(project);
    }

    public void updateStatus(Principal principal, Long id, ProjectStatus status) {

        // Fails if any of references are null
        if(id < 0) {
            log.error("Impossible to update project status {} : some references are missing", id);
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ProjectEntity project = projectRepository.findById(id).orElse(null);

        // Fails if any of references are null
        if(project == null) {
            log.error("Impossible to update project status {} : one or more reference(s) doesn't exist", id);
            throw new NotFoundException();
        }

        // Verify that principal has enough privileges
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        if(userService.isNotAdmin(userLoggedInId)
                && !userService.isManagerOfOrganization(userLoggedInId, project.getOrganization().getId())
                && !userLoggedInId.equals(project.getLeader().getId())) {
            log.error("Impossible to update project status {} : principal has not enough privileges", project.getId());
            throw new ForbiddenException();
        }

        // Save previous status for notification purposes
        ProjectStatus previousStatus = project.getStatus();

        // Update status
        project.setStatus(status);
        projectRepository.save(project);

        // Prepare & send notifications
        if(previousStatus.equals(ProjectStatus.DRAFT)
                && status.equals(ProjectStatus.IN_PROGRESS)) {
            Map<String, Object> model = new HashMap<>();
            model.put("user_fullname", userLoggedIn.getFullname());
            model.put("project_title", project.getTitle());
            model.put("project_url", webUrl + "/projects/" + project.getId());
            notificationService.create(NotificationName.PROJECT_PUBLISHED, model, project.getOrganization().getId());
        }
    }
}
