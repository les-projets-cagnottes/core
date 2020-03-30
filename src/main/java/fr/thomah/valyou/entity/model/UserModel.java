package fr.thomah.valyou.entity.model;

import fr.thomah.valyou.audit.AuditEntity;
import fr.thomah.valyou.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@MappedSuperclass
public class UserModel extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = "username")
    protected String username;

    @Column(name = "password")
    @NotNull
    protected String password;

    @Column(name = "email", unique = true)
    @NotNull
    protected String email;

    @Column(name = "firstname")
    protected String firstname = "";

    @Column(name = "lastname")
    protected String lastname = "";

    @Column(name = "avatarUrl")
    protected String avatarUrl;

    @Column(name = "enabled")
    @NotNull
    protected Boolean enabled = false;

    @Column(name = "lastpasswordresetdate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date lastPasswordResetDate = new Date();

    public static UserModel fromEntity(User user) {
        UserModel model = new UserModel();
        model.setCreatedAt(user.getCreatedAt());
        model.setCreatedBy(user.getCreatedBy());
        model.setUpdatedAt(user.getUpdatedAt());
        model.setUpdatedBy(user.getUpdatedBy());
        model.setId(user.getId());
        model.setUsername(user.getUsername());
        model.setPassword(user.getPassword());
        model.setEmail(user.getEmail());
        model.setFirstname(user.getFirstname());
        model.setLastname(user.getLastname());
        model.setAvatarUrl(user.getAvatarUrl());
        model.setEnabled(user.getEnabled());
        model.setLastPasswordResetDate(user.getLastPasswordResetDate());
        return model;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", enabled=" + enabled +
                ", lastPasswordResetDate=" + lastPasswordResetDate +
                '}';
    }

}
