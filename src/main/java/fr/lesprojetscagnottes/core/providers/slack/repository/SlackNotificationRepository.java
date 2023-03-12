package fr.lesprojetscagnottes.core.providers.slack.repository;

import fr.lesprojetscagnottes.core.providers.slack.entity.SlackNotificationEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackNotificationRepository extends JpaRepository<SlackNotificationEntity, Long> {
    SlackNotificationEntity findByNotificationId(Long id);

    @Transactional
    void deleteAllByTeamId(Long id);
}
