package fr.lesprojetscagnottes.core.scheduler;

import fr.lesprojetscagnottes.core.entity.Reminder;
import fr.lesprojetscagnottes.core.repository.ReminderRepository;
import fr.lesprojetscagnottes.core.task.SlackNotificationRunnableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderScheduler {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderScheduler.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ReminderRepository reminderRepository;

    public void schedule() {
        List<Reminder> reminders = reminderRepository.findAll();
        reminders.forEach(reminder -> {
            switch(reminder.getName()) {
                case IDEA:
                    taskScheduler.schedule(new SlackNotificationRunnableTask("Hello"), new CronTrigger(reminder.getPlanning()));
                    break;
                default:
                    break;
            }
        });
    }


}
