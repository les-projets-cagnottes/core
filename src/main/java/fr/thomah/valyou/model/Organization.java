package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
public class Organization extends AuditEntity<String>{

    private static final long serialVersionUID = -6671779784632800305L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    @NotNull
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "organizations_users",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private Set<User> members = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"leader", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties({"organization", "sponsor"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @OneToMany
    @JsonIgnoreProperties({"organization", "users"})
    private Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public Set<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(Set<Budget> budgets) {
        this.budgets = budgets;
    }

    public Set<OrganizationAuthority> getOrganizationAuthorities() {
        return organizationAuthorities;
    }

    public void setOrganizationAuthorities(Set<OrganizationAuthority> organizationAuthorities) {
        this.organizationAuthorities = organizationAuthorities;
    }

    public void addProject(Project project) {
        this.projects.add(project);
    }

}
