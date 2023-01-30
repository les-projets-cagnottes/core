package fr.lesprojetscagnottes.core.content.entity;

import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "contents")
public class ContentEntity extends ContentModel {

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

}
