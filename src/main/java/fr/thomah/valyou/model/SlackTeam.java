package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_team")
public class SlackTeam extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(mappedBy = "slackTeam")
    @JsonIgnoreProperties(value = {"slackTeam"})
    private Organization organization;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "bot_user_id")
    private String botUserId;

    @Column(name = "bot_access_token")
    private String botAccessToken;
}
