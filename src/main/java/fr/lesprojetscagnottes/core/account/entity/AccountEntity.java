package fr.lesprojetscagnottes.core.account.entity;

import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "accounts")
public class AccountEntity extends AccountModel {

    @ManyToOne
    private UserEntity owner = new UserEntity();

    @ManyToOne
    private BudgetEntity budget = new BudgetEntity();

    @OneToMany(mappedBy = "account")
    private Set<Donation> donations = new LinkedHashSet<>();

}
