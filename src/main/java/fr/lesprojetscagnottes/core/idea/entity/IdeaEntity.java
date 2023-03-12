package fr.lesprojetscagnottes.core.idea.entity;

import fr.lesprojetscagnottes.core.idea.model.IdeaModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
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
@Table(name = "ideas")
public class IdeaEntity extends IdeaModel {

    @ManyToOne(fetch = FetchType.LAZY)
    protected UserEntity submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationEntity organization = new OrganizationEntity();

}
