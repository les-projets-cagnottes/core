package fr.lesprojetscagnottes.core.providers.slack.entity;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.providers.slack.model.SlackNotificationModel;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private NotificationEntity notification = new NotificationEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    private SlackTeamEntity team = new SlackTeamEntity();

}
