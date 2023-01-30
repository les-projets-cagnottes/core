package fr.lesprojetscagnottes.core.providers.microsoft.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftNotificationEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder()
public class MicrosoftNotificationModel extends AuditEntity<String> {

    @Column
    protected Boolean sent;

    @Transient
    protected GenericModel notification;

    @Transient
    protected GenericModel team;

    public static MicrosoftNotificationModel fromEntity(MicrosoftNotificationEntity entity) {

        return MicrosoftNotificationModel.builder()
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
