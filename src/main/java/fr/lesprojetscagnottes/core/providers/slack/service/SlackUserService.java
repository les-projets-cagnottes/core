package fr.lesprojetscagnottes.core.providers.slack.service;

import fr.lesprojetscagnottes.core.providers.slack.entity.SlackUserEntity;
import fr.lesprojetscagnottes.core.providers.slack.repository.SlackUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackUserService {

    @Autowired
    private SlackUserRepository slackUserRepository;

    public void deleteAllBySlackTeamId(long id) {
        slackUserRepository.deleteAllBySlackTeamId(id);
    }

    public SlackUserEntity findBySlackId(String slackId) {
        return slackUserRepository.findBySlackId(slackId);
    }

    public SlackUserEntity findByUserIdAndSlackTeamId(Long userId, Long slackTeamId) {
        return slackUserRepository.findByUserIdAndSlackTeamId(userId, slackTeamId);
    }

    public SlackUserEntity save(SlackUserEntity slackUser) {
        return slackUserRepository.save(slackUser);
    }
}
