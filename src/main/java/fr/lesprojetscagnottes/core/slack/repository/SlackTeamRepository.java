package fr.lesprojetscagnottes.core.slack.repository;

import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackTeamRepository extends JpaRepository<SlackTeamEntity, Long> {
    SlackTeamEntity findByTeamId(String teamId);
}