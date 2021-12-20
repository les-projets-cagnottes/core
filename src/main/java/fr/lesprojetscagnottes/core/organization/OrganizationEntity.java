package fr.lesprojetscagnottes.core.organization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.idea.entity.IdeaEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
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
@NamedEntityGraph(name = "Organization.withLinkedEntities",
        attributeNodes = {
                @NamedAttributeNode("members"),
                @NamedAttributeNode("campaigns")
        }
)
@Table(name = "organizations")
public class OrganizationEntity extends OrganizationModel {

    @ManyToMany
    @JoinTable(
            name = "organizations_users",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private Set<UserEntity> members = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "projects_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organizations", "news"})
    private Set<ProjectEntity> projects = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "campaigns_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

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

    public void addProject(CampaignEntity campaign) {
        this.campaigns.add(campaign);
    }

}
