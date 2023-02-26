package fr.lesprojetscagnottes.core.news.controller;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.news.model.NewsModel;
import fr.lesprojetscagnottes.core.news.repository.NewsRepository;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "News", description = "The News API")
public class NewsController {

    @Autowired
    private Gson gson;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Find a news by its ID", description = "Find a news by its ID", tags = { "News" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the news", content = @Content(schema = @Schema(implementation = NewsModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "news not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/news/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public NewsModel findById(Principal principal, @PathVariable("id") Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            log.error("Impossible to get news by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        NewsEntity entity = newsRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to get news by ID : news not found");
            throw new NotFoundException();
        }

        // If the news is in an organization, verify that principal is in this organization
        if(entity.getOrganization() != null) {
            Long userLoggedInId = userService.get(principal).getId();
            OrganizationEntity newsOrganizations = organizationRepository.findByNews_Id(id);
            if(!userService.isMemberOfOrganization(userLoggedInId, newsOrganizations.getId()) && userService.isNotAdmin(userLoggedInId)) {
                log.error("Impossible to get news by ID : principal has not enough privileges");
                throw new ForbiddenException();
            }
        }

        // Transform and return organization
        return NewsModel.fromEntity(entity);
    }

    @Operation(summary = "Get list of News by a list of IDs", description = "Find a list of News by a list of IDs", tags = { "News" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the News", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NewsModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/news", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Set<NewsModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {

        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<NewsModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            NewsEntity news = newsRepository.findById(id).orElse(null);
            if(news == null) {
                log.error("Impossible to get news {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal is member of organization
            if(userService.isMemberOfOrganization(userLoggedInId, news.getOrganization().getId()) && userLoggedIn_isNotAdmin) {
                log.error("Impossible to get news {} : {} is not member of organization {}", id, userLoggedInId, news.getOrganization().getId());
                throw new ForbiddenException();
            }

            // Add the user to returned list
            models.add(NewsModel.fromEntity(news));
        }

        return models;
    }

    @Operation(summary = "Create a news", description = "Create a news", tags = { "News" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "News created", content = @Content(schema = @Schema(implementation = NewsModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/news", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NewsModel create(Principal principal, @RequestBody NewsModel news) {

        // Fails if any of references are null
        if(news == null) {
            log.error("Impossible to create a null news");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        OrganizationEntity organization = organizationRepository.findById(news.getOrganization().getId()).orElse(null);
        ProjectEntity project = projectRepository.findById(news.getProject().getId()).orElse(null);

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        if(organization != null && !userService.isMemberOfOrganization(userLoggedIn.getId(), organization.getId())) {
            log.error("Impossible to create news \"{}\" : principal {} is not member of organization {}", news.getTitle(), userLoggedIn.getId(), organization.getId());
            throw new ForbiddenException();
        }

        // Save news
        NewsEntity newsToSave = new NewsEntity();
        newsToSave.setType(news.getType());
        newsToSave.setTitle(news.getTitle());
        newsToSave.setContent(news.getContent());
        newsToSave.setWorkspace(news.getWorkspace());
        newsToSave.setAuthor(userLoggedIn);
        newsToSave.setOrganization(organization);
        newsToSave.setProject(project);
        return NewsModel.fromEntity(newsRepository.save(newsToSave));
    }

    @Operation(summary = "Update a news", description = "Update a news", tags = { "News" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News updated", content = @Content(schema = @Schema(implementation = NewsModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/news", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public NewsModel update(Principal principal, @RequestBody NewsModel newsModel) {

        // Fails if any of references are null
        if(newsModel == null || newsModel.getId() <= 0) {
            if(newsModel != null ) {
                log.error("Impossible to update news {} : some references are missing", newsModel.getId());
            } else {
                log.error("Impossible to update a null news");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        NewsEntity newsEntity = newsRepository.findById(newsModel.getId()).orElse(null);
        OrganizationEntity organization = organizationRepository.findById(newsModel.getOrganization().getId()).orElse(null);
        ProjectEntity project = projectRepository.findById(newsModel.getProject().getId()).orElse(null);

        // Verify that news exists
        if(newsEntity == null) {
            log.error("Impossible to get news {} : it doesn't exist", newsModel.getId());
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        if(organization != null && !userService.isMemberOfOrganization(userLoggedIn.getId(), organization.getId()) && userService.isNotAdmin(userLoggedIn.getId()) ) {
            log.error("Impossible to update news \"{}\" : principal {} is not member of organization {}", newsModel.getTitle(), userLoggedIn.getId(), organization.getId());
            throw new ForbiddenException();
        }

        // Save news
        newsEntity.setType(newsModel.getType());
        newsEntity.setTitle(newsModel.getTitle());
        newsEntity.setContent(newsModel.getContent());
        newsEntity.setWorkspace(newsModel.getWorkspace());
        newsEntity.setOrganization(newsModel.getOrganization());
        newsEntity.setProject(project);
        return NewsModel.fromEntity(newsRepository.save(newsEntity));
    }

}
