package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.entity.model.AccountModel;
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
public class Account extends AccountModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private User owner = new User();

    @ManyToOne
    @JsonIgnoreProperties(value = {"organization", "sponsor", "donations", "accounts"})
    private Budget budget = new Budget();

    @OneToMany
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

}
