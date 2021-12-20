package fr.lesprojetscagnottes.core.project.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
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

    @Column
    private String workspace;

    @Transient
    protected GenericModel leader;

    @Transient
    protected GenericModel organization;

    @Transient
    private Set<Long> campaignsRef = new LinkedHashSet<>();

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
        model.setWorkspace(entity.getWorkspace());
        model.setLeader(new GenericModel(entity.getLeader()));
        model.setOrganization(new GenericModel(entity.getOrganization()));
        entity.getCampaigns().forEach(campaign -> model.getCampaignsRef().add(campaign.getId()));
        entity.getPeopleGivingTime().forEach(member -> model.getPeopleGivingTimeRef().add(member.getId()));
        entity.getNews().forEach(news -> model.getNewsRef().add(news.getId()));
        return model;
    }

}
