package fr.lesprojetscagnottes.core.providers.microsoft.service;

import com.google.gson.Gson;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.notification.model.NotificationVariables;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftNotificationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MicrosoftBotService {

    @Value("${fr.lesprojetscagnottes.microsoft.client_id}")
    private String microsoftClientId;

    @Value("${fr.lesprojetscagnottes.microsoft.client_secret}")
    private String microsoftClientSecret;

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    @Autowired
    private Gson gson;

    @Autowired
    private BotFrameworkHttpAdapter adapter;

    @Autowired
    private MicrosoftTeamService microsoftTeamService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private UserService userService;

    public void hello(MicrosoftTeamModel msTeam) {
        UserEntity orgAdminUser = userService.findByEmail(msTeam.getUpdatedBy());

        Map<String, Object> model = new HashMap<>();
        model.put("URL", webUrl);
        model.put("contact", "<b>" + orgAdminUser.getFirstname() + " " + orgAdminUser.getLastname() + "</b>");

        Context context = new Context();
        context.setVariables(model);
        String teamsMessage = templateEngine.process("microsoft/fr/HELLO", context);

        Activity msActivity = Activity.createMessageActivity();
        msActivity.setText(teamsMessage);

        TeamsChannelData channelData = new TeamsChannelData();
        channelData.setTeamsTeamId(msTeam.getGroupId());
        channelData.setTeamsChannelId(msTeam.getChannelId());

        ConversationParameters conversationParameters = new ConversationParameters();
        conversationParameters.setIsGroup(true);
        conversationParameters.setChannelData(channelData);
        conversationParameters.setTenantId(msTeam.getTenantId());
        conversationParameters.setActivity(msActivity);

        MicrosoftAppCredentials appCredentials = new MicrosoftAppCredentials(microsoftClientId, microsoftClientSecret, msTeam.getTenantId());

        adapter.createConversation(
                msTeam.getChannelId(),
                "https://smba.trafficmanager.net/emea/",
                appCredentials,
                conversationParameters,
                null
        );
    }

    public void hello(Principal principal, Long id) {

        // Fails if Team ID is missing
        if(id <= 0) {
            log.error("Impossible to send hello world message to ms team: ID is incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        MicrosoftTeamEntity msTeam = microsoftTeamService.findById(id);

        // Verify that any of references are not null
        if(msTeam == null ) {
            log.error("Impossible to send hello world message to ms team : team {} not found", id);
            throw new NotFoundException();
        }

        // Verify that Slack Team is associated with an organization
        OrganizationEntity organization = msTeam.getOrganization();
        if(organization == null ) {
            log.error("Impossible to send hello world message to ms team : no organization is associated with Slack Team");
            throw new NotFoundException();
        }

        // Verify that principal has correct privileges :
        // Principal is owner of the organization OR Principal is admin
        Long userLoggedInId = userService.get(principal).getId();
        if(!userService.isOwnerOfOrganization(userLoggedInId, organization.getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to send hello world message to ms team : principal {} has not enough privileges", userLoggedInId);
            throw new ForbiddenException();
        }

        hello(msTeam);
    }

    public void sendNotification(NotificationEntity notification, MicrosoftNotificationEntity msNotification) {
        Context context = new Context();
        context.setVariables(gson.fromJson(notification.getVariables(), NotificationVariables.class));
        String teamsMessage = templateEngine.process("microsoft/fr/" + notification.getName(), context);

        Activity msActivity = Activity.createMessageActivity();
        msActivity.setText(teamsMessage);

        TeamsChannelData channelData = new TeamsChannelData();
        MicrosoftTeamEntity msTeam = msNotification.getTeam();
        channelData.setTeamsTeamId(msTeam.getGroupId());
        channelData.setTeamsChannelId(msTeam.getChannelId());

        ConversationParameters conversationParameters = new ConversationParameters();
        conversationParameters.setIsGroup(true);
        conversationParameters.setChannelData(channelData);
        conversationParameters.setTenantId(msTeam.getTenantId());
        conversationParameters.setActivity(msActivity);

        MicrosoftAppCredentials appCredentials = new MicrosoftAppCredentials(microsoftClientId, microsoftClientSecret, msTeam.getTenantId());

        adapter.createConversation(
                msTeam.getChannelId(),
                "https://smba.trafficmanager.net/emea/",
                appCredentials,
                conversationParameters,
                null
        );
    }

}
