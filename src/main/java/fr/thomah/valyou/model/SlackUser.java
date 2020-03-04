package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_user")
public class SlackUser extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers"})
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"organization", "slackUsers"})
    private SlackTeam slackTeam;

    @Column
    private String slackId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String imId;

    @Column
    private String image_192;

    @Column
    private Boolean deleted;

}
