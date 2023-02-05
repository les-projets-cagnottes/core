package fr.lesprojetscagnottes.core.authentication.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.authentication.AuthenticationResponseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@MappedSuperclass
public class AuthenticationResponseModel extends AuditEntity<String> {

    @Column
    protected String description = StringsCommon.EMPTY_STRING;

    @Column
    protected Date expiration = new Date();

    @Column
    protected String token = StringsCommon.EMPTY_STRING;

    public AuthenticationResponseModel() {}

    public AuthenticationResponseModel(String token) {
        this.token = token;
    }

    public static AuthenticationResponseModel fromEntity(AuthenticationResponseEntity authenticationResponse) {
        AuthenticationResponseModel model = new AuthenticationResponseModel();
        model.setCreatedAt(authenticationResponse.getCreatedAt());
        model.setCreatedBy(authenticationResponse.getCreatedBy());
        model.setUpdatedAt(authenticationResponse.getUpdatedAt());
        model.setUpdatedBy(authenticationResponse.getUpdatedBy());
        model.setId(authenticationResponse.getId());
        model.setDescription(authenticationResponse.getDescription());
        model.setExpiration(authenticationResponse.getExpiration());
        model.setToken(authenticationResponse.getToken());
        return model;
    }
}