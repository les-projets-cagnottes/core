package fr.thomah.valyou.model.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public class AuthenticationRequest {
    private String email;
    private String password;
}