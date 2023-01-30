package fr.lesprojetscagnottes.core.authorization.model;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class AuthorityModel extends AuditEntity<String> {

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    protected AuthorityName name;

    public static AuthorityModel fromEntity(AuthorityEntity entity) {
        AuthorityModel model = new AuthorityModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        return model;
    }
}
