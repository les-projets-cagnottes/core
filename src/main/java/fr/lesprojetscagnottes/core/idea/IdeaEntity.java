package fr.lesprojetscagnottes.core.idea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.content.entity.TagEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
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
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    protected UserEntity submitter;

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private OrganizationEntity organization = new OrganizationEntity();

    @ManyToMany
    @JoinTable(
            name = "ideas_users",
            joinColumns = {@JoinColumn(name = "idea_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    private Set<UserEntity> followers = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "ideas_tags",
            joinColumns = {@JoinColumn(name = "idea_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"organization", "ideas"})
    private Set<TagEntity> tags = new LinkedHashSet<>();

}
