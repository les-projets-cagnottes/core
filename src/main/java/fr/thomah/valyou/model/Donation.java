package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class Donation extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "amount")
    @NotNull
    private float amount;

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
