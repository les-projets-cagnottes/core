package fr.lesprojetscagnottes.core.authentication;

import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "api_tokens")
public class AuthenticationResponseEntity extends AuthenticationResponseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1250166508152483573L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Date expiration = new Date();

    @ManyToOne
    private UserEntity user = new UserEntity();

    public AuthenticationResponseEntity() {}

    public AuthenticationResponseEntity(String token) {
        this.token = token;
    }

}
