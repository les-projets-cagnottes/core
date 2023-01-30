package fr.lesprojetscagnottes.core.idea.entity;

import fr.lesprojetscagnottes.core.idea.model.IdeaModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ideas")
public class IdeaEntity extends IdeaModel {

    @ManyToOne
    protected UserEntity submitter;

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

}
