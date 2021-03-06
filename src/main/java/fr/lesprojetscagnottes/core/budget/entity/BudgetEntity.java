package fr.lesprojetscagnottes.core.budget.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.campaign.CampaignEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
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
public class BudgetEntity extends BudgetModel {

    @ManyToMany
    @JoinTable(
            name = "campaigns_budgets",
            joinColumns = {@JoinColumn(name = "budget_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"organizations"})
    private ContentEntity rules = new ContentEntity();

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private UserEntity sponsor = new UserEntity();

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = {"owner", "budget"})
    private Set<AccountEntity> accounts = new LinkedHashSet<>();

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
