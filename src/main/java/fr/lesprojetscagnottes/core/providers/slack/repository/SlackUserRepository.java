package fr.lesprojetscagnottes.core.providers.slack.repository;

import fr.lesprojetscagnottes.core.providers.slack.entity.SlackUserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SlackUserRepository extends JpaRepository<SlackUserEntity, Long> {
    Set<SlackUserEntity> findAllBySlackTeamId(Long id);

    SlackUserEntity findBySlackId(String slackUserId);

    @Transactional
    void deleteAllBySlackTeamId(Long slackTeamId);

    SlackUserEntity findByUserIdAndSlackTeamId(Long userId, Long slackTeamId);
}