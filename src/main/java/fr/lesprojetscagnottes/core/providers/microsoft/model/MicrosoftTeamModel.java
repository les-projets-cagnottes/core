package fr.lesprojetscagnottes.core.providers.microsoft.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class MicrosoftTeamModel extends AuditEntity<String> {

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "tenant_id")
    private String tenantId;

    @Transient
    protected GenericModel organization;

    public static MicrosoftTeamModel fromEntity(MicrosoftTeamModel entity) {
        MicrosoftTeamModel model = new MicrosoftTeamModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setAccessToken(entity.getAccessToken());
        model.setTenantId(entity.getTenantId());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        return model;
    }

}
