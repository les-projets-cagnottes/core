package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.ProjectModel;
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
public class Project extends ProjectModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private User leader = new User();

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Campaign> campaigns = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "projects_members",
            joinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    private Set<User> peopleGivingTime = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "projects", "budgets", "contents"})
    private Set<Organization> organizations = new LinkedHashSet<>();

}
