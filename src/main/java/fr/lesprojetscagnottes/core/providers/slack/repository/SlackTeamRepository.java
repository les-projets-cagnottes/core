package fr.lesprojetscagnottes.core.providers.slack.repository;

import fr.lesprojetscagnottes.core.providers.slack.entity.SlackTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackTeamRepository extends JpaRepository<SlackTeamEntity, Long> {
    SlackTeamEntity findByTeamId(String teamId);
    SlackTeamEntity findByOrganizationId(Long id);

}