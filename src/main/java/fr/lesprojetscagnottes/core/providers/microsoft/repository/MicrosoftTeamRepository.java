package fr.lesprojetscagnottes.core.providers.microsoft.repository;

import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicrosoftTeamRepository extends JpaRepository<MicrosoftTeamEntity, Long> {
    MicrosoftTeamEntity findByOrganizationId(Long id);
}