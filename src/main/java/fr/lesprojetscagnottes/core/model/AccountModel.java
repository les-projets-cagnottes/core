package fr.lesprojetscagnottes.core.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class AccountModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountModel.class);

    @Column(name = "initial_amount")
    @NotNull
    protected float initialAmount;

    @Column(name = "amount")
    @NotNull
    protected float amount;

    @Transient
    protected GenericModel owner;

    @Transient
    protected GenericModel budget;

    public static AccountModel fromEntity(Account entity) {
        AccountModel model = new AccountModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setInitialAmount(entity.getInitialAmount());
        model.setAmount(entity.getAmount());
        model.setOwner(new GenericModel(entity.getOwner()));
        model.setBudget(new GenericModel(entity.getBudget()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "id=" + id +
                ", amount=" + amount +
                ", owner=" + owner +
                ", budget=" + budget +
                '}';
    }
}
