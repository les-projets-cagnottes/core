package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.OrganizationModel;
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
public class Organization extends OrganizationModel {

    @ManyToMany
    @JoinTable(
            name = "organizations_users",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private Set<User> members = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "projects_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "campaigns_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Campaign> campaigns = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<Idea> ideas = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "organizations_contents",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "content_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"organization"})
    private Set<Content> contents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"organization", "users"})
    private Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();

    @OneToOne(mappedBy = "organization")
    @JsonIgnoreProperties(value = {"organization", "slackUsers"})
    private SlackTeam slackTeam;

    public void addProject(Campaign campaign) {
        this.campaigns.add(campaign);
    }

}
