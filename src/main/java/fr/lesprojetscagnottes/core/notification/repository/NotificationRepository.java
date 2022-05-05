package fr.lesprojetscagnottes.core.notification.repository;

import fr.lesprojetscagnottes.core.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
