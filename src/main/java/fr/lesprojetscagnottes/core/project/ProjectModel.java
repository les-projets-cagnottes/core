package fr.lesprojetscagnottes.core.project;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.common.GenericModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class ProjectModel extends AuditEntity<String> {

    @Column(name = "title")
    @NotNull
    protected String title = StringsCommon.EMPTY_STRING;

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    protected ProjectStatus status;

    @Column(name = "short_description")
    protected String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    protected String longDescription;

    @Column(name = "people_required")
    protected Integer peopleRequired;

    @Transient
    protected GenericModel leader;

    @Transient
    private Set<Long> campaignsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> organizationsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> peopleGivingTimeRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> newsRef = new LinkedHashSet<>();

    public static ProjectModel fromEntity(ProjectEntity entity) {
        ProjectModel model = new ProjectModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setStatus(entity.getStatus());
        model.setShortDescription(entity.getShortDescription());
        model.setLongDescription(entity.getLongDescription());
        model.setPeopleRequired(entity.getPeopleRequired());
        model.setLeader(new GenericModel(entity.getLeader()));
        entity.getCampaigns().forEach(campaign -> model.getCampaignsRef().add(campaign.getId()));
        entity.getOrganizations().forEach(organization -> model.getOrganizationsRef().add(organization.getId()));
        entity.getPeopleGivingTime().forEach(member -> model.getPeopleGivingTimeRef().add(member.getId()));
        entity.getNews().forEach(news -> model.getNewsRef().add(news.getId()));
        return model;
    }

    @Override
    public String toString() {
        return "CampaignModel{" + "title='" + title + '\'' +
                ", status=" + status +
                ", shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", peopleRequired=" + peopleRequired +
                ", leader=" + leader +
                ", id=" + id +
                '}';
    }
}
