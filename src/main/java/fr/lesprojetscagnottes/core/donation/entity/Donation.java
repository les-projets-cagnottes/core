package fr.lesprojetscagnottes.core.donation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
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
    private UserEntity contributor = new UserEntity();

    @ManyToOne
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private CampaignEntity campaign = new CampaignEntity();

    @ManyToOne
    @JsonIgnoreProperties(value = {"owner", "budget"})
    private AccountEntity account = new AccountEntity();

    @Override
    public String toString() {
        return "Donation{" + "contributor=" + contributor.getId() +
                ", campaign=" + campaign.getId() +
                ", account=" + account.getId() +
                ", amount=" + amount +
                ", id=" + id +
                '}';
    }
}
