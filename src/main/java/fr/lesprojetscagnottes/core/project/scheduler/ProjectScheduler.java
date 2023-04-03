package fr.lesprojetscagnottes.core.project.scheduler;

import fr.lesprojetscagnottes.core.common.date.DateUtils;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.news.service.NewsService;
import fr.lesprojetscagnottes.core.notification.model.NotificationName;
import fr.lesprojetscagnottes.core.notification.service.NotificationService;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectStatus;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ProjectScheduler {

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    private final NewsService newsService;

    private final NotificationService notificationService;

    private final UserService userService;

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectScheduler(NewsService newsService,
                            NotificationService notificationService,
                            UserService userService,
                            ProjectRepository projectRepository) {
        this.newsService = newsService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    @Scheduled(cron = "${fr.lesprojetscagnottes.core.schedule.newsproject}")
    public void notifyCallForNews() {
        log.info("Calling for news has started");
        Set<ProjectStatus> status = new LinkedHashSet<>();
        status.add(ProjectStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowMinus2Months = now.minusMonths(2);
        Date nowMinus2MonthsDate = DateUtils.asDate(nowMinus2Months);
        Set<ProjectEntity> projects = projectRepository.findAllByStatusInAndLastStatusUpdateLessThan(status, nowMinus2MonthsDate);
        projects.forEach(project -> {
            NewsEntity news = newsService.findFirstByProjectIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(project.getId(), nowMinus2MonthsDate);
            if(news == null) {
                UserEntity leader = userService.findById(project.getLeader().getId());
                Map<String, Object> model = new HashMap<>();
                model.put("_user_email_", leader.getEmail());
                model.put("_organization_id_", project.getOrganization().getId());
                model.put("user_fullname", leader.getFullname());
                model.put("project_title", project.getTitle());
                model.put("project_url", webUrl + "/projects/" + project.getId());
                notificationService.create(NotificationName.PROJECT_CALL_FOR_NEWS, model, project.getOrganization().getId());
            }
        });
    }
}
