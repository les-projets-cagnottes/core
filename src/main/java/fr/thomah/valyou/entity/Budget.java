package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.entity.model.BudgetModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "budgets")
public class Budget extends BudgetModel {

    @ManyToMany
    @JoinTable(
            name = "project_budgets",
            joinColumns = {@JoinColumn(name = "budget_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"organizations"})
    private Content rules = new Content();

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "projects", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private Organization organization = new Organization();

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private User sponsor = new User();

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = {"budget"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "Budget{" +
                "projects=" + projects.size() +
                ", organization=" + organization.getId() +
                ", sponsor=" + sponsor.getId() +
                ", rules=" + rules.getId() +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", amountPerMember=" + amountPerMember +
                ", isDistributed=" + isDistributed +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalDonations=" + totalDonations +
                ", donations=" + donations.size() +
                '}';
    }
}
