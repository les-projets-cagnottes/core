package fr.lesprojetscagnottes.core.scheduler;

import fr.lesprojetscagnottes.core.entity.Reminder;
import fr.lesprojetscagnottes.core.repository.ReminderRepository;
import fr.lesprojetscagnottes.core.service.NotificationService;
import fr.lesprojetscagnottes.core.task.IdeaReminderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class ReminderScheduler {

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderRepository reminderRepository;

    public void schedule() {
        List<Reminder> reminders = reminderRepository.findAllByEnabled(Boolean.TRUE);
        reminders.forEach(this::start);
    }

    public void start(Reminder reminder) {
        switch(reminder.getType()) {
            case IDEA:
                IdeaReminderTask task = new IdeaReminderTask(reminder, notificationService);
                ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(reminder.getPlanning()));
                this.scheduledTasks.put(reminder.getId(), future);
                break;
            default:
                break;
        }
    }

    public void stop(Reminder reminder) {
        ScheduledFuture<?> future = this.scheduledTasks.get(reminder.getId());
        if(future != null) {
            future.cancel(false);
        }
    }

    public Map<Long, Boolean> statuses(Set<Long> ids) {
        Map<Long, Boolean> statuses = new HashMap<>();
        ids.forEach(id -> {
            ScheduledFuture<?> future = scheduledTasks.get(id);
            if(future != null) {
                statuses.put(id, !future.isCancelled() && !future.isDone());
            }
        });
        return statuses;
    }
}
