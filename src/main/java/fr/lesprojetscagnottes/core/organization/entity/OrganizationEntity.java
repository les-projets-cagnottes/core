package fr.lesprojetscagnottes.core.organization.entity;

import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.idea.entity.IdeaEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.organization.model.OrganizationModel;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
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
    private Set<UserEntity> members = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private Set<ProjectEntity> projects = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private Set<BudgetEntity> budgets = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private Set<IdeaEntity> ideas = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private Set<NewsEntity> news = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private Set<ContentEntity> contents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE)
    private Set<OrganizationAuthorityEntity> organizationAuthorities = new LinkedHashSet<>();

    @OneToOne(mappedBy = "organization")
    private SlackTeamEntity slackTeam;

}
