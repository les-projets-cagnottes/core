package fr.lesprojetscagnottes.core.content.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.content.model.TagModel;
import fr.lesprojetscagnottes.core.idea.IdeaEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class TagEntity extends TagModel {

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private OrganizationEntity organization = new OrganizationEntity();

    @ManyToMany(mappedBy = "tags")
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<IdeaEntity> ideas = new LinkedHashSet<>();

}
