package fr.lesprojetscagnottes.core.donation.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.common.GenericModel;
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
    protected float amount;

    @Transient
    protected GenericModel account;

    @Transient
    protected GenericModel contributor;

    @Transient
    protected GenericModel campaign;

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
                ", id=" + id +
                '}';
    }
}
