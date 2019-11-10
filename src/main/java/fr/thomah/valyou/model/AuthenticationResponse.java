package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "api_tokens")
public class AuthenticationResponse extends AuditEntity<String> implements Serializable {

    private static final long serialVersionUID = 1250166508152483573L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Date expiration = new Date();

    @Column
    private String token = "";

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private User user = new User();

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String token) {
        this.token = token;
    }

}
