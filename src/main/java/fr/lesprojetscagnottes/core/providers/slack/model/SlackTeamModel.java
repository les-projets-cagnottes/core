package fr.lesprojetscagnottes.core.providers.slack.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder()
public class SlackTeamModel extends AuditEntity<String> {

    @Transient
    protected GenericModel organization;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "team_id")
    private String teamId;
    @Column(name = "team_name")
    private String teamName;
    @Column(name = "bot_id")
    private String botId;
    @Column(name = "bot_user_id")
    private String botUserId;
    @Column(name = "bot_access_token")
    private String botAccessToken;
    @Column(name = "publication_channel")
    private String publicationChannel;
    @Column(name = "publication_channel_id")
    private String publicationChannelId;

    public static SlackTeamModel fromEntity(SlackTeamEntity entity) {

        return SlackTeamModel.builder()
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .id(entity.getId())
                .accessToken(entity.getAccessToken())
                .teamId(entity.getTeamId())
                .teamName(entity.getTeamName())
                .botId(entity.getBotId())
                .botUserId(entity.getBotUserId())
                .botAccessToken(entity.getBotAccessToken())
                .publicationChannel(entity.getPublicationChannel())
                .publicationChannelId(entity.getPublicationChannelId())
                .organization(new GenericModel(entity.getOrganization())).build();
    }

}
