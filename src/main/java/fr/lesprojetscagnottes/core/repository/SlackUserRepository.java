package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.SlackUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {
    SlackUser findBySlackId(String slackUserId);

    void deleteAllBySlackTeamId(Long slackTeamId);
}