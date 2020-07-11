package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.CampaignModel;
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
public class Campaign extends CampaignModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private User leader = new User();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "campaigns_members",
            joinColumns = {@JoinColumn(name = "campaign_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "followedIdeas"})
    private Set<User> peopleGivingTime = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "campaigns", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "campaigns", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "campaigns", "budgets", "contents"})
    private Set<Organization> organizations = new LinkedHashSet<>();

    public void addPeopleGivingTime(User user) {
        this.peopleGivingTime.add(user);
        user.getCampaigns().add(this);
    }

}
