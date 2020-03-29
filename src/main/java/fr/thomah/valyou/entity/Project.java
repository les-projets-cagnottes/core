package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "projects")
public class Project extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    @NotNull
    private String title = "";

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "donations_required")
    private Float donationsRequired;

    @Column(name = "peopleRequired")
    private Integer peopleRequired;

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private User leader = new User();

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date fundingDeadline = new Date();

    private Float totalDonations;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"contributor", "budget", "project"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_users_time",
            joinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private Set<User> peopleGivingTime = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"organization", "projects", "sponsor", "donations"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "projects", "budgets", "contents"})
    private Set<Organization> organizations = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", donationsRequired=" + donationsRequired +
                ", peopleRequired=" + peopleRequired +
                ", leader=" + leader +
                ", fundingDeadline=" + fundingDeadline +
                '}';
    }

    public void addPeopleGivingTime(User user) {
        this.peopleGivingTime.add(user);
        user.getProjects().add(this);
    }

    public void removePeopleGivingTime(User user) {
        this.peopleGivingTime.remove(user);
        user.getProjects().remove(this);
    }

    public void addBudget(Budget budget) {
        this.budgets.add(budget);
        budget.getProjects().add(this);
    }

    public void removeBudget(Budget budget) {
        this.budgets.remove(budget);
        budget.getProjects().remove(this);
    }

    public void addOrganization(Organization organization) {
        this.organizations.add(organization);
        organization.getProjects().add(this);
    }

    public void removeOrganization(Organization organization) {
        this.organizations.remove(organization);
        organization.getProjects().remove(this);
    }
}
