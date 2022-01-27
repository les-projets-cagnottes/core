package fr.lesprojetscagnottes.core.providers.microsoft.repository;

import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicrosoftUserRepository extends JpaRepository<MicrosoftUserEntity, Long> {
    MicrosoftUserEntity findByMail(String slackUserId);
}