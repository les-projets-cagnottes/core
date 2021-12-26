package fr.lesprojetscagnottes.core.organization.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.idea.entity.IdeaEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.organization.model.OrganizationModel;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
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
@Table(name = "organizations")
public class OrganizationEntity extends OrganizationModel {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "organizations_users",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private Set<UserEntity> members = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organization", "news"})
    private Set<ProjectEntity> projects = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private Set<BudgetEntity> budgets = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<IdeaEntity> ideas = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties({"author", "organization", "project"})
    private Set<NewsEntity> news = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization"})
    private Set<ContentEntity> contents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"organization", "users"})
    private Set<OrganizationAuthorityEntity> organizationAuthorities = new LinkedHashSet<>();

    @OneToOne(mappedBy = "organization")
    @JsonIgnoreProperties(value = {"organization", "slackUsers"})
    private SlackTeamEntity slackTeam;

}
