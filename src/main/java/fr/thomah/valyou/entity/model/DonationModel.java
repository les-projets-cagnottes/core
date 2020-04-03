package fr.thomah.valyou.entity.model;

import fr.thomah.valyou.audit.AuditEntity;
import fr.thomah.valyou.entity.Budget;
import fr.thomah.valyou.entity.Donation;
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
    private GenericModel contributor;

    @Transient
    private GenericModel project;

    @Transient
    private GenericModel budget = new Budget();

    public static DonationModel fromEntity(Donation entity) {
        DonationModel model = new DonationModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setAmount(entity.getAmount());
        model.setContributor(new GenericModel(entity.getContributor()));
        model.setProject(new GenericModel(entity.getProject()));
        model.setBudget(new GenericModel(entity.getBudget()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

    @Override
    public String toString() {
        return "DonationModel{" +
                "id=" + id +
                ", amount=" + amount +
                ", contributor=" + contributor +
                ", project=" + project +
                ", budget=" + budget +
                '}';
    }
}
