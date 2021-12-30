package fr.lesprojetscagnottes.core.budget.entity;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
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

    @ManyToOne
    private ContentEntity rules = new ContentEntity();

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.REMOVE)
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

    @ManyToOne
    private UserEntity sponsor = new UserEntity();

    @OneToMany(mappedBy = "budget")
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
