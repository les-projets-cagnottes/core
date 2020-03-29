package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.SlackUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {
    SlackUser findBySlackId(String slackUserId);
}