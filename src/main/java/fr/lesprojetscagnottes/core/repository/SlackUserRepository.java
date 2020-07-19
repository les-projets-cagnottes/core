package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.SlackUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {
    Set<SlackUser> findAllBySlackTeamId(Long id);

    SlackUser findBySlackId(String slackUserId);

    void deleteAllBySlackTeamId(Long slackTeamId);

}