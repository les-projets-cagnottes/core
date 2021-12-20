package fr.lesprojetscagnottes.core.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.campaign.model.CampaignModel;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "campaigns")
public class CampaignEntity extends CampaignModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organizations", "news"})
    private ProjectEntity project = new ProjectEntity();

    @ManyToOne
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private BudgetEntity budget = new BudgetEntity();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

}
