package fr.lesprojetscagnottes.core.providers.microsoft;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.notification.service.NotificationService;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftNotificationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftBotService;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftNotificationService;
import fr.lesprojetscagnottes.core.providers.microsoft.service.MicrosoftTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class MicrosoftNotificationScheduler {

    @Value("${fr.lesprojetscagnottes.microsoft.enabled}")
    private boolean msEnabled;

    @Autowired
    private MicrosoftBotService microsoftBotService;

    @Autowired
    private MicrosoftNotificationService microsoftNotificationService;

    @Autowired
    private MicrosoftTeamService microsoftTeamService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processNotifications() {
        if(msEnabled) {
            List<NotificationEntity> notifications = notificationService.list();
            notifications.forEach(notification -> {
                log.debug("Process notification {}", notification.getId());
                MicrosoftNotificationEntity msNotification = microsoftNotificationService.findByNotificationId(notification.getId());
                if(msNotification == null) {
                    log.debug("No MS notification detected");
                    msNotification = new MicrosoftNotificationEntity();
                    msNotification.setNotification(notification);
                    msNotification.setSent(false);
                    msNotification.setTeam(microsoftTeamService.findByOrganizationId(notification.getOrganization().getId()));
                    msNotification = microsoftNotificationService.save(msNotification);
                }
                if(!msNotification.getSent()) {
                    log.debug("Sending MS notification {}", msNotification.getId());
                    microsoftBotService.sendNotification(notification, msNotification);
                    msNotification.setSent(true);
                    microsoftNotificationService.save(msNotification);
                }
            });
        }
    }

}
