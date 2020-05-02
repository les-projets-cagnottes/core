package fr.lesprojetscagnottes.core.entity.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.AuthenticationResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@MappedSuperclass
public class AuthenticationResponseModel extends AuditEntity<String> {

    @Column
    protected Date expiration = new Date();

    @Column
    protected String token = StringsCommon.EMPTY_STRING;

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