package fr.lesprojetscagnottes.core.entity.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class UserModel extends AuditEntity<String> {

    @Column(name = "username")
    protected String username;

    @Column(name = "password")
    @NotNull
    protected String password;

    @Column(name = "email", unique = true)
    @NotNull
    protected String email;

    @Column(name = "firstname")
    protected String firstname = StringsCommon.EMPTY_STRING;

    @Column(name = "lastname")
    protected String lastname = StringsCommon.EMPTY_STRING;

    @Column(name = "avatarUrl")
    protected String avatarUrl;

    @Column(name = "enabled")
    @NotNull
    protected Boolean enabled = false;

    @Column(name = "lastpasswordresetdate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date lastPasswordResetDate = new Date();

    public static UserModel fromEntity(User entity) {
        UserModel model = new UserModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setUsername(entity.getUsername());
        model.setEmail(entity.getEmail());
        model.setFirstname(entity.getFirstname());
        model.setLastname(entity.getLastname());
        model.setAvatarUrl(entity.getAvatarUrl());
        model.setEnabled(entity.getEnabled());
        model.setLastPasswordResetDate(entity.getLastPasswordResetDate());
        return model;
    }

    public void emptyPassword() {
        this.password = "***";
    }

}
