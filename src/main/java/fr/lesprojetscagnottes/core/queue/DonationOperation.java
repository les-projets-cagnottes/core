package fr.lesprojetscagnottes.core.stack;

import fr.lesprojetscagnottes.core.entity.Donation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class DonationOperation {

    private DonationOperationType type;
    private Donation donation;

}
