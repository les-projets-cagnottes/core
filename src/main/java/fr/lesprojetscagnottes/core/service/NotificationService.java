package fr.lesprojetscagnottes.core.service;

import fr.lesprojetscagnottes.core.entity.SlackUser;
import fr.lesprojetscagnottes.core.repository.SlackTeamRepository;
import fr.lesprojetscagnottes.core.repository.SlackUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service(value = "notificationService")
public class NotificationService {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    public void notifyAllSlackUsers(String templateName)  {
        slackTeamRepository.findAll().forEach(slackTeam -> {
            Set<SlackUser> slackUsers = slackUserRepository.findAllBySlackTeamId(slackTeam.getId());

            long delay;
            long tsAfterPostMessage = (new Timestamp(System.currentTimeMillis())).getTime();
            for(SlackUser slackUser : slackUsers) {
                try {
                    Map<String, Object> model = new HashMap<>();
                    model.put("organization", slackTeam.getOrganization());
                    model.put("baseUrl", WEB_URL);

                    Context context = new Context();
                    context.setVariables(model);
                    String slackMessage = templateEngine.process(templateName, context);

                    // Slack chat.postMessage method is Web API Special (1 per second) so wait 1000ms
                    delay = (new Timestamp(System.currentTimeMillis())).getTime() - tsAfterPostMessage;
                    if(delay > 1000) {
                        delay = 1000;
                    }
                    Thread.sleep(1000 - delay);

                    LOGGER.info("[notification-slack][{}][{}] - " + slackMessage, slackTeam.getTeamId(), slackUser.getImId());
                    slackClientService.postMessage(slackTeam, slackUser.getImId(), slackMessage);
                    LOGGER.info("[notification-slack][{}][{}] - Sent", slackTeam.getTeamId(), slackUser.getImId());

                    tsAfterPostMessage = (new Timestamp(System.currentTimeMillis())).getTime();
                } catch (InterruptedException e) {
                    LOGGER.error("Can't sleep Thread");
                }
            }
        });
    }
}