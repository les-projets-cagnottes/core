package fr.lesprojetscagnottes.core.notification;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.common.strings.ScheduleParamsCommon;
import fr.lesprojetscagnottes.core.schedule.ScheduleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ReminderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderTask.class);

    private ScheduleEntity reminder;
    private NotificationService notificationService;

    public ReminderTask(ScheduleEntity reminder, NotificationService notificationService){
        this.notificationService = notificationService;
        this.reminder = reminder;
    }

    @Override
    public void run() {
        LOGGER.info("Start IdeaReminderTask on thread " + Thread.currentThread().getName());
        Gson gson = new Gson();
        Map<String, String> params = gson.fromJson(reminder.getParams(), Map.class);
        notificationService.notifyAllSlackUsers(params.get(ScheduleParamsCommon.SLACK_TEMPLATE));
    }
}