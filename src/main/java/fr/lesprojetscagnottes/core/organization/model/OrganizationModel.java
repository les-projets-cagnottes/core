package fr.lesprojetscagnottes.core.organization.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class OrganizationModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationModel.class);

    @Column
    @NotNull
    private String name;

    @Column
    @NotNull
    private String socialName;

    @Column(name = "logo_url")
    private String logoUrl;

    @Transient
    private GenericModel slackTeam;

    @Transient
    private Set<Long> contentsRef = new LinkedHashSet<>();

    @Transient
    private Set<Long> membersRef = new LinkedHashSet<>();

    public static OrganizationModel fromEntity(OrganizationEntity entity) {
        OrganizationModel model = new OrganizationModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setLogoUrl(entity.getLogoUrl());
        model.setSlackTeam(new GenericModel(entity.getSlackTeam()));
        entity.getMembers().forEach(member -> model.getMembersRef().add(member.getId()));
        entity.getContents().forEach(content -> model.getContentsRef().add(content.getId()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

    @Override
    public String toString() {
        return "OrganizationModel{" +
                "name='" + name + '\'' +
                ", slackTeam=" + slackTeam +
                ", id=" + id +
                '}';
    }
}
