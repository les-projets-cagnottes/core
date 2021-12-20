package fr.lesprojetscagnottes.core.budget.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.budget.model.AccountModel;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "accounts")
public class AccountEntity extends AccountModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private UserEntity owner = new UserEntity();

    @ManyToOne
    @JsonIgnoreProperties(value = {"organization", "sponsor", "donations", "accounts"})
    private BudgetEntity budget = new BudgetEntity();

    @OneToMany(mappedBy = "account")
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

}
