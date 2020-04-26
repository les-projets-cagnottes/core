package fr.lesprojetscagnottes.core.entity.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.entity.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.entity.OrganizationAuthority;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class OrganizationAuthorityModel extends AuditEntity<String> {

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    protected OrganizationAuthorityName name;

    @Transient
    protected GenericModel organization = new GenericModel();

    public static OrganizationAuthorityModel fromEntity(OrganizationAuthority entity) {
        OrganizationAuthorityModel model = new OrganizationAuthorityModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        return model;
    }

    @Override
    public String toString() {
        return "OrganizationAuthorityModel{" +
                "name=" + name +
                ", organization=" + organization +
                ", id=" + id +
                '}';
    }
}
