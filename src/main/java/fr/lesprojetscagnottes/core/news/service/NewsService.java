package fr.lesprojetscagnottes.core.news.service;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.news.model.NewsModel;
import fr.lesprojetscagnottes.core.news.repository.NewsRepository;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.service.ProjectService;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
public class NewsService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private NewsRepository newsRepository;

    public DataPage<NewsModel> listByProjects_Id(Principal principal, Long id, int offset, int limit) {

        // Verify that IDs are corrects
        if(id <= 0) {
            log.error("Impossible to get news : parameters are incorrect");
            throw new BadRequestException();
        }

        // Verify that organization exists
        ProjectEntity project = projectService.findById(id);
        if(project == null) {
            log.error("Impossible to get news : project not found");
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        Long userLoggedInId = userService.get(principal).getId();
        if(userService.isNotAdmin(userLoggedInId) && !userService.isMemberOfOrganization(userLoggedInId, project.getOrganization().getId())) {
            log.error("Impossible to get news : principal is not a member of any of the project organizations");
            throw new ForbiddenException();
        }

        // Get and transform entities
        Page<NewsEntity> entities = newsRepository.findAllByProjectId(id, PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
        DataPage<NewsModel> models = new DataPage<>(entities);
        entities.getContent().forEach(entity -> models.getContent().add(NewsModel.fromEntity(entity)));
        return models;
    }

}
