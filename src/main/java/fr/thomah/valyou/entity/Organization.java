package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@NamedEntityGraph(name = "Organization.withMembers",
        attributeNodes = {
                @NamedAttributeNode("members")
        }
)
@Table(name = "organizations")
public class Organization extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    @NotNull
    private String name;

    @ManyToMany
    @JoinTable(
            name = "organizations_users",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private Set<User> members = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization", "projects", "sponsor", "donations"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "organizations_contents",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "content_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"organization"})
    private Set<Content> contents = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"organization", "users"})
    private Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();

    @OneToOne(mappedBy = "organization")
    @JsonIgnoreProperties(value = {"organization", "slackUsers"})
    private SlackTeam slackTeam;

    public void addProject(Project project) {
        this.projects.add(project);
    }

}