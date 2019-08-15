package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations")
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Organization extends AuditEntity {

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
    private List<User> members = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "project_organizations",
            joinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    private List<Project> projects = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

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

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public void addProject(Project project) {
        this.projects.add(project);
    }

    public void addMember(User user) {
        this.members.add(user);
    }
}
