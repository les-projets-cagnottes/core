package fr.lesprojetscagnottes.core.entity.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.entity.Donation;
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
public class DonationModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationModel.class);

    @Column(name = "amount")
    @NotNull
    private float amount;

    @Transient
    private GenericModel account;

    @Transient
    private GenericModel contributor;

    @Transient
    private GenericModel campaign;

    @Transient
    private GenericModel budget;

    public static DonationModel fromEntity(Donation entity) {
        DonationModel model = new DonationModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setAmount(entity.getAmount());
        model.setAccount(new GenericModel(entity.getAccount()));
        model.setContributor(new GenericModel(entity.getContributor()));
        model.setCampaign(new GenericModel(entity.getCampaign()));
        model.setBudget(new GenericModel(entity.getBudget()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

    @Override
    public String toString() {
        return "DonationModel{" +
                "amount=" + amount +
                ", account=" + account +
                ", contributor=" + contributor +
                ", campaign=" + campaign +
                ", budget=" + budget +
                ", id=" + id +
                '}';
    }
}
