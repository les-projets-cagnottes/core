package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.SlackTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackTeamRepository extends JpaRepository<SlackTeam, Long> {
    SlackTeam findByTeamId(String teamId);
}