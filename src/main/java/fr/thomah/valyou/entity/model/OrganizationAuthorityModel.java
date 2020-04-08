package fr.thomah.valyou.entity.model;

import fr.thomah.valyou.audit.AuditEntity;
import fr.thomah.valyou.entity.OrganizationAuthorityName;
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

    @Override
    public String toString() {
        return "OrganizationAuthorityModel{" +
                "name=" + name +
                ", organization=" + organization +
                ", id=" + id +
                '}';
    }
}
