package fr.lesprojetscagnottes.core.campaign;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.common.GenericModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class CampaignModel extends AuditEntity<String> {

    @Column(name = "title")
    @NotNull
    protected String title = StringsCommon.EMPTY_STRING;

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    protected CampaignStatus status;

    @Column(name = "short_description")
    protected String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    protected String longDescription;

    @Column(name = "donations_required")
    protected Float donationsRequired;

    @Column(name = "people_required")
    protected Integer peopleRequired;

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date fundingDeadline = new Date();

    @Column(name = "total_donations")
    @NotNull
    protected Float totalDonations = 0f;

    @Transient
    protected GenericModel project;

    @Transient
    protected GenericModel leader;

    @Transient
    private Set<Long> organizationsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> budgetsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> peopleGivingTimeRef = new LinkedHashSet<>();

    public static CampaignModel fromEntity(CampaignEntity entity) {
        CampaignModel model = new CampaignModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setStatus(entity.getStatus());
        model.setShortDescription(entity.getShortDescription());
        model.setLongDescription(entity.getLongDescription());
        model.setDonationsRequired(entity.getDonationsRequired());
        model.setPeopleRequired(entity.getPeopleRequired());
        model.setFundingDeadline(entity.getFundingDeadline());
        model.setTotalDonations(entity.getTotalDonations());
        model.setLeader(new GenericModel(entity.getLeader()));
        model.setProject(new GenericModel(entity.getProject()));
        entity.getOrganizations().forEach(organization -> model.getOrganizationsRef().add(organization.getId()));
        entity.getBudgets().forEach(budget -> model.getBudgetsRef().add(budget.getId()));
        entity.getPeopleGivingTime().forEach(member -> model.getPeopleGivingTimeRef().add(member.getId()));
        return model;
    }

    @Override
    public String toString() {
        return "CampaignModel{" + "title='" + title + '\'' +
                ", status=" + status +
                ", shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", donationsRequired=" + donationsRequired +
                ", peopleRequired=" + peopleRequired +
                ", fundingDeadline=" + fundingDeadline +
                ", totalDonations=" + totalDonations +
                ", leader=" + leader +
                ", id=" + id +
                '}';
    }
}
