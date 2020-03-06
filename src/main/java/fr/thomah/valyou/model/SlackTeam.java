package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_team")
public class SlackTeam extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"members", "projects", "budgets", "slackTeam"})
    private Organization organization = new Organization();

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization", "slackTeam", "user"})
    private Set<SlackUser> slackUsers = new LinkedHashSet<>();

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "bot_user_id")
    private String botUserId;

    @Column(name = "bot_access_token")
    private String botAccessToken;

    @Column(name = "publication_channel")
    private String publicationChannel;
}
