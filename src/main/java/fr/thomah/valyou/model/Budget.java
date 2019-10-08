package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "budgets")
public class Budget extends AuditEntity<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name = "";

    @Column(name = "amount_per_member")
    @NotNull
    private float amountPerMember = 0f;

    @Column(name = "is_distributed")
    @NotNull
    private Boolean isDistributed = false;

    @Column(name = "start_date")
    @NotNull
    private Date startDate = new Date();

    @Column(name = "end_date")
    @NotNull
    private Date endDate = new Date();

    @ManyToMany
    @JoinTable(
            name = "project_budgets",
            joinColumns = {@JoinColumn(name = "budget_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"organizations"})
    private Content rules;

    @ManyToOne
    @JsonIgnoreProperties({"members", "projects", "budgets"})
    private Organization organization;

    @ManyToOne
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private User sponsor;

    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties({"budget"})
    private Set<Donation> donations = new LinkedHashSet<>();

}
