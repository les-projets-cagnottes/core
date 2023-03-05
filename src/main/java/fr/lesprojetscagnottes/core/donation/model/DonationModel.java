package fr.lesprojetscagnottes.core.donation.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class DonationModel extends AuditEntity<String> {

    @Column(name = "amount")
    @NotNull
    protected float amount;

    @Transient
    protected GenericModel account;

    @Transient
    protected GenericModel campaign;

    public static DonationModel fromEntity(DonationEntity entity) {
        DonationModel model = new DonationModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setAmount(entity.getAmount());
        model.setAccount(new GenericModel(entity.getAccount()));
        model.setCampaign(new GenericModel(entity.getCampaign()));
        return model;
    }

    @Override
    public String toString() {
        return "DonationModel{" +
                "amount=" + amount +
                ", account=" + account +
                ", campaign=" + campaign +
                ", id=" + id +
                '}';
    }
}
