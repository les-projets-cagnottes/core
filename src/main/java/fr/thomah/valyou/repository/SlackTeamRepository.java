package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.SlackTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackTeamRepository extends JpaRepository<SlackTeam, Long> {
    SlackTeam findByTeamId(String teamId);
}