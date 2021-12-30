package fr.lesprojetscagnottes.core.idea.entity;

import fr.lesprojetscagnottes.core.idea.model.IdeaModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ideas")
public class IdeaEntity extends IdeaModel {

    @ManyToOne
    protected UserEntity submitter;

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

    @ManyToMany
    @JoinTable(
            name = "ideas_users",
            joinColumns = {@JoinColumn(name = "idea_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    private Set<UserEntity> followers = new LinkedHashSet<>();

}
