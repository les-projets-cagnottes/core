package fr.lesprojetscagnottes.core.providers.slack.repository;

import fr.lesprojetscagnottes.core.providers.slack.entity.SlackNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackNotificationRepository extends JpaRepository<SlackNotificationEntity, Long> {
    SlackNotificationEntity findByNotificationId(Long id);
}
