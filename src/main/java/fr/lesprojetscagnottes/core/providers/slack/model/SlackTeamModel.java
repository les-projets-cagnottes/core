package fr.lesprojetscagnottes.core.providers.slack.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.GenericModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class SlackTeamModel extends AuditEntity<String> {

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "bot_user_id")
    private String botUserId;

    @Column(name = "bot_access_token")
    private String botAccessToken;

    @Column(name = "publication_channel")
    private String publicationChannel;

    @Column(name = "publication_channel_id")
    private String publicationChannelId;

    @Transient
    protected GenericModel organization;

    public static SlackTeamModel fromEntity(SlackTeamModel entity) {
        SlackTeamModel model = new SlackTeamModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setAccessToken(entity.getAccessToken());
        model.setTeamId(entity.getTeamId());
        model.setTeamName(entity.getTeamName());
        model.setBotUserId(entity.getBotUserId());
        model.setBotAccessToken(entity.getBotAccessToken());
        model.setPublicationChannel(entity.getPublicationChannel());
        model.setPublicationChannelId(entity.getPublicationChannelId());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        return model;
    }

}
