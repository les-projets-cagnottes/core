package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.BudgetModel;
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
            name = "campaigns_budgets",
            joinColumns = {@JoinColumn(name = "budget_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Campaign> campaigns = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"organizations"})
    private Content rules = new Content();

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private Organization organization = new Organization();

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private User sponsor = new User();

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = {"owner", "budget"})
    private Set<Account> accounts = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "Budget{" +
                "campaigns=" + campaigns.size() +
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
                '}';
    }
}
