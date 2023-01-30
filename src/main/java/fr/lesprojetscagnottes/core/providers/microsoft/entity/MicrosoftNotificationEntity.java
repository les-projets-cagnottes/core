package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftNotificationModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ms_notifications")
public class MicrosoftNotificationEntity extends MicrosoftNotificationModel {

    @ManyToOne
    private NotificationEntity notification = new NotificationEntity();

    @ManyToOne
    private MicrosoftTeamEntity team = new MicrosoftTeamEntity();

}
