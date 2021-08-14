package fr.lesprojetscagnottes.core.notification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.lesprojetscagnottes.core.common.strings.ScheduleParamsCommon;
import fr.lesprojetscagnottes.core.schedule.ScheduleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public record ReminderTask(ScheduleEntity reminder,
                           NotificationService notificationService) implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderTask.class);

    @Override
    public void run() {
        LOGGER.info("Start IdeaReminderTask on thread " + Thread.currentThread().getName());
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> params = gson.fromJson(reminder.getParams(), type);
        notificationService.notifyAllSlackUsers(params.get(ScheduleParamsCommon.SLACK_TEMPLATE));
    }
}