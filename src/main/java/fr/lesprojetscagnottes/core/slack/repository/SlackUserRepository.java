package fr.lesprojetscagnottes.core.slack.repository;

import fr.lesprojetscagnottes.core.slack.entity.SlackUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SlackUserRepository extends JpaRepository<SlackUserEntity, Long> {
    Set<SlackUserEntity> findAllBySlackTeamId(Long id);

    SlackUserEntity findBySlackId(String slackUserId);

    void deleteAllBySlackTeamId(Long slackTeamId);

}