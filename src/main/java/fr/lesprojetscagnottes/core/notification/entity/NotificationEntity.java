package fr.lesprojetscagnottes.core.notification.entity;

import fr.lesprojetscagnottes.core.notification.model.NotificationModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "notifications")
public class NotificationEntity extends NotificationModel {

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

}
