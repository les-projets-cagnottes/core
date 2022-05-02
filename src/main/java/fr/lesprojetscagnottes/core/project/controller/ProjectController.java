package fr.lesprojetscagnottes.core.project.controller;

import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.news.model.NewsModel;
import fr.lesprojetscagnottes.core.news.service.NewsService;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
import fr.lesprojetscagnottes.core.project.model.ProjectStatus;
import fr.lesprojetscagnottes.core.project.service.ProjectService;
import fr.lesprojetscagnottes.core.user.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Projects", description = "The Projects API")
public class ProjectController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private ProjectService projectService;

    @Operation(summary = "Find a project by its ID", description = "Find a project by its ID", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the project", content = @Content(schema = @Schema(implementation = ProjectModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectModel findById(Principal principal, @PathVariable("id") Long id) {
        return projectService.findById(principal, id);
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
        return projectService.getByIds(principal, ids);
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
        return newsService.listByProjects_Id(principal, id, offset, limit);
    }

    @Operation(summary = "Get teammates", description = "Get teammates of a project", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding users", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Params are incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/teammates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<UserModel> listTeammates(Principal principal, @PathVariable("id") Long id) {
        return projectService.listTeammates(principal, id);
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
        return projectService.create(principal, project);
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
        return projectService.update(principal, projectModel);
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
        projectService.join(principal, id);
    }

    @Operation(summary = "Update the status of a project", description = "Update the project with the given status", tags = { "Projects" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated"),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/project/{id}/status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateStatus(Principal principal, @PathVariable("id") Long id, @RequestBody String status) {
        projectService.updateStatus(principal, id, ProjectStatus.valueOf(status));
    }

}
