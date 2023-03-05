package fr.lesprojetscagnottes.core.donation.queue;

import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class DonationOperation {

    private DonationOperationType type;
    private DonationEntity donation;

    public DonationOperation(DonationEntity donation, DonationOperationType type) {
        this.donation = donation;
        this.type = type;
    }

}
