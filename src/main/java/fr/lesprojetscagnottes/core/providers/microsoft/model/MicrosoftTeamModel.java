package fr.lesprojetscagnottes.core.providers.microsoft.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class MicrosoftTeamModel extends AuditEntity<String> {

    @Column
    protected String displayName;

    @Column
    protected String groupId;

    @Column
    protected String channelId;

    @Column
    protected String tenantId;

    @Column
    protected String companyFilter;

    @Transient
    protected GenericModel organization;

    @Transient
    private Set<Long> msUsersRef = new LinkedHashSet<>();

    public static MicrosoftTeamModel fromEntity(MicrosoftTeamEntity entity) {
        MicrosoftTeamModel model = new MicrosoftTeamModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setDisplayName(entity.getDisplayName());
        model.setGroupId(entity.getGroupId());
        model.setChannelId(entity.getChannelId());
        model.setTenantId(entity.getTenantId());
        model.setCompanyFilter(entity.getCompanyFilter());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        entity.getMsUsers().forEach(msUser -> model.getMsUsersRef().add(msUser.getId()));
        return model;
    }

}
