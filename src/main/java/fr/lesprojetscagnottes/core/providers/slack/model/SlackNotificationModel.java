package fr.lesprojetscagnottes.core.providers.slack.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackNotificationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder()
public class SlackNotificationModel extends AuditEntity<String> {

    @Column
    protected Boolean sent;

    @Transient
    protected GenericModel notification;

    @Transient
    protected GenericModel team;

    public static SlackNotificationModel fromEntity(SlackNotificationEntity entity) {

        return SlackNotificationModel.builder()
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .id(entity.getId())
                .notification(new GenericModel(entity.getNotification()))
                .team(new GenericModel(entity.getTeam()))
                .build();
    }
}
