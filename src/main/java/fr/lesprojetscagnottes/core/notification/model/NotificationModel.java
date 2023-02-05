package fr.lesprojetscagnottes.core.notification.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder()
public class NotificationModel extends AuditEntity<String> {

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    protected NotificationName name;

    @Column
    protected String variables;

    @Transient
    protected GenericModel organization;

    @Transient
    protected GenericModel user;

    public static NotificationModel fromEntity(NotificationEntity entity) {

        return NotificationModel.builder()
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .id(entity.getId())
                .name(entity.getName())
                .variables(entity.getVariables())
                .organization(new GenericModel(entity.getOrganization()))
                .build();
    }
}
