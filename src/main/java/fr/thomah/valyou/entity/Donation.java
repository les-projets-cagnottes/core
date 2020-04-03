package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.entity.model.DonationModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class Donation extends DonationModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private User contributor = new User();

    @ManyToOne
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Project project = new Project();

    @ManyToOne
    @JsonIgnoreProperties(value = {"organization", "sponsor", "donations"})
    private Budget budget = new Budget();

}
