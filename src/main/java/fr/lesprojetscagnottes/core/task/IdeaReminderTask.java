package fr.lesprojetscagnottes.core.task;

import fr.lesprojetscagnottes.core.entity.Reminder;
import fr.lesprojetscagnottes.core.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdeaReminderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdeaReminderTask.class);

    private Reminder reminder;
    private NotificationService notificationService;

    public IdeaReminderTask(Reminder reminder, NotificationService notificationService){
        this.notificationService = notificationService;
        this.reminder = reminder;
    }

    @Override
    public void run() {
        LOGGER.info("Start IdeaReminderTask on thread " + Thread.currentThread().getName());
        notificationService.notifyAllSlackUsers("slack/fr/idea-reminder");
    }
}