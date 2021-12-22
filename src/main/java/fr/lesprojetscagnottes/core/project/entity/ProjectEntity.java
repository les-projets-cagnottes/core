package fr.lesprojetscagnottes.core.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
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
@Table(name = "projects")
public class ProjectEntity extends ProjectModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens", "news"})
    private UserEntity leader = new UserEntity();

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "projects_members",
            joinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    private Set<UserEntity> peopleGivingTime = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = {"members", "projects", "budgets", "contents"})
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(
            mappedBy = "project",
            orphanRemoval = true)
    @JsonIgnoreProperties({"author", "organization", "project"})
    private Set<NewsEntity> news = new LinkedHashSet<>();

}
