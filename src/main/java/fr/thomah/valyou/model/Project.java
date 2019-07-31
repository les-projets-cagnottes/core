package fr.thomah.valyou.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    @NotNull
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description")
    private String longDescription;

    @ManyToOne(cascade = CascadeType.ALL)
    private User leader;

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date fundingDeadline = new Date();

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Donation> donations = new ArrayList<>();

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "project_users_time",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    private List<User> peopleGivingTime = new ArrayList<>();

    public Project() {
    }

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

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }

    public List<User> getPeopleGivingTime() {
        return peopleGivingTime;
    }

    public void setPeopleGivingTime(List<User> peopleGivingTime) {
        this.peopleGivingTime = peopleGivingTime;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", leader=" + leader +
                ", fundingDeadline=" + fundingDeadline +
                '}';
    }
}
