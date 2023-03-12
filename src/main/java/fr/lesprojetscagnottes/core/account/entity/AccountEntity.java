package fr.lesprojetscagnottes.core.account.entity;

import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "accounts")
public class AccountEntity extends AccountModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity owner = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    private BudgetEntity budget = new BudgetEntity();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<DonationEntity> donations = new LinkedHashSet<>();

}
