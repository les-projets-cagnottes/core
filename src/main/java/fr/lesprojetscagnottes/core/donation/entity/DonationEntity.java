package fr.lesprojetscagnottes.core.donation.entity;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class DonationEntity extends DonationModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private CampaignEntity campaign = new CampaignEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    private AccountEntity account = new AccountEntity();

}
