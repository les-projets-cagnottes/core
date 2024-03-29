package fr.lesprojetscagnottes.core.organization.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@NotNull
public class OrganizationModel extends AuditEntity<String> {

    @Column
    @NotNull
    @NotEmpty
    @JsonProperty(required = true)
    private String name;

    @Column
    private String socialName;

    @URL
    @Column(name = "logo_url")
    private String logoUrl;

    @Transient
    private GenericModel slackTeam;

    @Transient
    private GenericModel msTeam;

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
        model.setSocialName(entity.getSocialName());
        model.setLogoUrl(entity.getLogoUrl());
        model.setSlackTeam(new GenericModel(entity.getSlackTeam()));
        model.setMsTeam(new GenericModel(entity.getMsTeam()));
        entity.getMembers().forEach(member -> model.getMembersRef().add(member.getId()));
        entity.getContents().forEach(content -> model.getContentsRef().add(content.getId()));
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
