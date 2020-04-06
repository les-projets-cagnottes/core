package fr.thomah.valyou.entity.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public class AuthenticationRequestModel {
    private String email;
    private String password;
}