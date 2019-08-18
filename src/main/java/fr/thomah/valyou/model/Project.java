package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "projects")
public class Project extends AuditEntity {

    private static final long serialVersionUID = -661039969628937779L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    @NotNull
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "donations_required")
    private Float donationsRequired;

    @Column(name = "peopleRequired")
    private Integer peopleRequired;

    @ManyToOne
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private User leader;

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date fundingDeadline = new Date();

    private Float totalDonations;

    @OneToMany(
            mappedBy = "project",
            orphanRemoval = true)
    @JsonIgnoreProperties({"budget"})
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
    @JsonIgnoreProperties({"members", "projects", "budgets"})
    private Set<Organization> organizations = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Float getDonationsRequired() {
        return donationsRequired;
    }

    public void setDonationsRequired(Float donationsRequired) {
        this.donationsRequired = donationsRequired;
    }

    public Integer getPeopleRequired() {
        return peopleRequired;
    }

    public void setPeopleRequired(Integer peopleRequired) {
        this.peopleRequired = peopleRequired;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public Date getFundingDeadline() {
        return fundingDeadline;
    }

    public void setFundingDeadline(Date fundingDeadline) {
        this.fundingDeadline = fundingDeadline;
    }

    public void setTotalDonations(Float totalDonations) {
        this.totalDonations = totalDonations;
    }

    public Set<Donation> getDonations() {
        return donations;
    }

    public void setDonations(Set<Donation> donations) {
        this.donations = donations;
    }

    public Set<User> getPeopleGivingTime() {
        return peopleGivingTime;
    }

    public void setPeopleGivingTime(Set<User> peopleGivingTime) {
        this.peopleGivingTime = peopleGivingTime;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<Organization> organizations) {
        this.organizations = organizations;
    }

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

    public void addOrganization(Organization organization) {
        this.organizations.add(organization);
    }
}
