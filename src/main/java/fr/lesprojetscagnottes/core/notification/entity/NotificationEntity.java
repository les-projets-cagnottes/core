package fr.lesprojetscagnottes.core.notification.entity;

import fr.lesprojetscagnottes.core.notification.model.NotificationModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import jakarta.persistence.FetchType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "notifications")
public class NotificationEntity extends NotificationModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationEntity organization = new OrganizationEntity();

}
