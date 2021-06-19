package fr.lesprojetscagnottes.core.campaign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.project.ProjectEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
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
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens", "news"})
    private UserEntity leader = new UserEntity();

    @ManyToOne
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organizations", "news"})
    private ProjectEntity project = new ProjectEntity();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "campaigns_members",
            joinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    private Set<UserEntity> peopleGivingTime = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "campaigns", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private Set<BudgetEntity> budgets = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "campaigns", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "campaigns", "budgets", "contents"})
    private Set<OrganizationEntity> organizations = new LinkedHashSet<>();

    public void addPeopleGivingTime(UserEntity user) {
        this.peopleGivingTime.add(user);
        user.getCampaigns().add(this);
    }

}
