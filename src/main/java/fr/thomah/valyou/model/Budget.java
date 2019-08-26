package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "budgets")
public class Budget extends AuditEntity<String>{

    private static final long serialVersionUID = -8233663715686887295L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "amount_per_member")
    @NotNull
    private Float amountPerMember;

    @Column(name = "is_distributed")
    @NotNull
    private Boolean isDistributed = false;

    @Column(name = "start_date")
    @NotNull
    private Date startDate;

    @Column(name = "end_date")
    @NotNull
    private Date endDate;

    @ManyToOne
    @JsonIgnoreProperties({"members", "projects", "budgets"})
    private Organization organization;

    @ManyToOne
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private User sponsor;

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties({"budget"})
    private Set<Donation> donations = new LinkedHashSet<>();

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

    public Float getAmountPerMember() {
        return amountPerMember;
    }

    public void setAmountPerMember(Float amountPerMember) {
        this.amountPerMember = amountPerMember;
    }

    public Boolean getDistributed() {
        return isDistributed;
    }

    public void setDistributed(Boolean distributed) {
        isDistributed = distributed;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getSponsor() {
        return sponsor;
    }

    public void setSponsor(User sponsor) {
        this.sponsor = sponsor;
    }

    public Set<Donation> getDonations() {
        return donations;
    }

    public void setDonations(Set<Donation> donations) {
        this.donations = donations;
    }
}
