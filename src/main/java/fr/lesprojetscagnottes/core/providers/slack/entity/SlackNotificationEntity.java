package fr.lesprojetscagnottes.core.providers.slack.entity;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.providers.slack.model.SlackNotificationModel;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_notifications")
public class SlackNotificationEntity extends SlackNotificationModel {

    @ManyToOne
    private NotificationEntity notification = new NotificationEntity();

    @ManyToOne
    private SlackTeamEntity team = new SlackTeamEntity();

}
