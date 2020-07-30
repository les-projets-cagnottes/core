package fr.lesprojetscagnottes.core.scheduler;

import fr.lesprojetscagnottes.core.entity.Schedule;
import fr.lesprojetscagnottes.core.repository.ScheduleRepository;
import fr.lesprojetscagnottes.core.service.NotificationService;
import fr.lesprojetscagnottes.core.task.ReminderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class MainScheduler {

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void schedule() {
        List<Schedule> reminders = scheduleRepository.findAllByEnabled(Boolean.TRUE);
        reminders.forEach(this::start);
    }

    public void start(Schedule schedule) {
        switch(schedule.getType()) {
            case REMINDER:
                ReminderTask task = new ReminderTask(schedule, notificationService);
                ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(schedule.getPlanning()));
                this.scheduledTasks.put(schedule.getId(), future);
                break;
            default:
                break;
        }
    }

    public void stop(Schedule reminder) {
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
