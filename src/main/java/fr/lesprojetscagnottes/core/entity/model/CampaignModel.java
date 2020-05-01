package fr.lesprojetscagnottes.core.entity.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.CampaignStatus;
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
    private String title = StringsCommon.EMPTY_STRING;

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "donations_required")
    private Float donationsRequired;

    @Column(name = "peopleRequired")
    private Integer peopleRequired;

    @Column(name = "funding_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date fundingDeadline = new Date();

    @Column(name = "total_donations")
    private Float totalDonations;

    @Transient
    private GenericModel leader;

    @Transient
    private Set<Long> organizationsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> budgetsRef = new LinkedHashSet<>();

    public static CampaignModel fromEntity(Campaign entity) {
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
        return model;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CampaignModel{");
        sb.append("title='").append(title).append('\'');
        sb.append(", status=").append(status);
        sb.append(", shortDescription='").append(shortDescription).append('\'');
        sb.append(", longDescription='").append(longDescription).append('\'');
        sb.append(", donationsRequired=").append(donationsRequired);
        sb.append(", peopleRequired=").append(peopleRequired);
        sb.append(", fundingDeadline=").append(fundingDeadline);
        sb.append(", totalDonations=").append(totalDonations);
        sb.append(", leader=").append(leader);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
