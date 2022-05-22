package fr.lesprojetscagnottes.core.providers.microsoft.service;

import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftNotificationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.repository.MicrosoftNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MicrosoftNotificationService {

    @Autowired
    private MicrosoftNotificationRepository microsoftNotificationRepository;

    public MicrosoftNotificationEntity findByNotificationId(Long id) {
        return microsoftNotificationRepository.findByNotificationId(id);
    }

    public MicrosoftNotificationEntity save(MicrosoftNotificationEntity msNotification) {
        return microsoftNotificationRepository.save(msNotification);
    }
}
