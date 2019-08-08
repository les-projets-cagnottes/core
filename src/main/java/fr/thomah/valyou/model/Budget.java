package fr.thomah.valyou.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget extends AuditEntity {

    private static final long serialVersionUID = -8233663715686887295L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "amount_per_member")
    @NotNull
    private Float amountPerMember;

    @ManyToOne
    private Organization organization;

    @ManyToOne
    private User sponsor;

    @OneToMany(mappedBy = "budget")
    private List<Donation> donations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getAmountPerMember() {
        return amountPerMember;
    }

    public void setAmountPerMember(Float amountPerMember) {
        this.amountPerMember = amountPerMember;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getSponsor() {
        return sponsor;
    }

    public void setSponsor(User sponsor) {
        this.sponsor = sponsor;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }
}
