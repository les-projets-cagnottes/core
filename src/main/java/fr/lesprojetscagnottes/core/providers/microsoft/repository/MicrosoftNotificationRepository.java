package fr.lesprojetscagnottes.core.providers.microsoft.repository;

import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicrosoftNotificationRepository extends JpaRepository<MicrosoftNotificationEntity, Long> {
    MicrosoftNotificationEntity findByNotificationId(Long id);
}
