package fr.thomah.valyou.entity.model;

import fr.thomah.valyou.audit.AuditEntity;
import fr.thomah.valyou.entity.AuthenticationResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@MappedSuperclass
public class AuthenticationResponseModel extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Date expiration = new Date();

    @Column
    private String token = "";

    public AuthenticationResponseModel() {}

    public AuthenticationResponseModel(String token) {
        this.token = token;
    }

    public static AuthenticationResponseModel fromEntity(AuthenticationResponse authenticationResponse) {
        AuthenticationResponseModel model = new AuthenticationResponseModel();
        model.setCreatedAt(authenticationResponse.getCreatedAt());
        model.setCreatedBy(authenticationResponse.getCreatedBy());
        model.setUpdatedAt(authenticationResponse.getUpdatedAt());
        model.setUpdatedBy(authenticationResponse.getUpdatedBy());
        model.setId(authenticationResponse.getId());
        model.setExpiration(authenticationResponse.getExpiration());
        model.setToken(authenticationResponse.getToken());
        return model;
    }
}