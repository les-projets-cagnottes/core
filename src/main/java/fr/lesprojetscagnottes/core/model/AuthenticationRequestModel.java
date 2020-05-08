package fr.lesprojetscagnottes.core.entity.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public class AuthenticationRequestModel {
    protected String email;
    protected String password;
}