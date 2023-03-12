package fr.lesprojetscagnottes.core.campaign.entity;

import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.campaign.model.CampaignModel;
import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "campaigns")
public class CampaignEntity extends CampaignModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectEntity project = new ProjectEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    private BudgetEntity budget = new BudgetEntity();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<DonationEntity> donations = new LinkedHashSet<>();

}
