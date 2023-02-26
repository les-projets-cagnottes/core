package fr.lesprojetscagnottes.core.providers.slack;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.notification.service.NotificationService;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackNotificationEntity;
import fr.lesprojetscagnottes.core.providers.slack.service.SlackClientService;
import fr.lesprojetscagnottes.core.providers.slack.service.SlackNotificationService;
import fr.lesprojetscagnottes.core.providers.slack.service.SlackTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class SlackNotificationScheduler {

    @Value("${fr.lesprojetscagnottes.microsoft.enabled}")
    private boolean msEnabled;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private SlackNotificationService slackNotificationService;

    @Autowired
    private SlackTeamService slackTeamService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processNotifications() {
        if(msEnabled) {
            List<NotificationEntity> notifications = notificationService.list();
            notifications.forEach(notification -> {
                log.debug("Process notification {}", notification.getId());
                SlackNotificationEntity slackNotification = slackNotificationService.findByNotificationId(notification.getId());
                if(slackNotification == null) {
                    log.debug("No Slack notification detected");
                    slackNotification = new SlackNotificationEntity();
                    slackNotification.setNotification(notification);
                    slackNotification.setSent(false);
                    slackNotification.setTeam(slackTeamService.findByOrganizationId(notification.getOrganization().getId()));
                    slackNotification = slackNotificationService.save(slackNotification);
                }
                if(!slackNotification.getSent()) {
                    log.debug("Sending Slack notification {}", slackNotification.getId());
                    slackClientService.sendNotification(notification, slackNotification);
                    slackNotification.setSent(true);
                    slackNotificationService.save(slackNotification);
                }
            });
        }
    }

}
