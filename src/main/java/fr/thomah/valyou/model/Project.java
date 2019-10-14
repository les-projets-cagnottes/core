package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private User leader = new User();

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date fundingDeadline = new Date();

    private Float totalDonations;

    @OneToMany(mappedBy = "project")
    @JsonIgnoreProperties(value = {"contributor", "budget", "project"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
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

    public Float getTotalDonations() {
        totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        return totalDonations;
    }

    public void addPeopleGivingTime(User user) {
        this.peopleGivingTime.add(user);
    }

}
