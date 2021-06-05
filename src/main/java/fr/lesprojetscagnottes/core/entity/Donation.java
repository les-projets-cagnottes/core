package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.DonationModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class Donation extends DonationModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private User contributor = new User();

    @ManyToOne
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Campaign campaign = new Campaign();

    @ManyToOne
    @JsonIgnoreProperties(value = {"organization", "sponsor", "donations", "accounts"})
    private Budget budget = new Budget();

    @ManyToOne
    @JsonIgnoreProperties(value = {"owner", "budget"})
    private Account account = new Account();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Donation{");
        sb.append("contributor=").append(contributor.getId());
        sb.append(", campaign=").append(campaign.getId());
        sb.append(", budget=").append(budget.getId());
        sb.append(", account=").append(account.getId());
        sb.append(", amount=").append(amount);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
