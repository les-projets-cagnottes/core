package fr.lesprojetscagnottes.core.providers.microsoft.service;

import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.repository.MicrosoftUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MicrosoftUserService {

    @Autowired
    private MicrosoftUserRepository microsoftUserRepository;

    public MicrosoftUserEntity getByMsId(String msId) {
        return microsoftUserRepository.findByMsId(msId);
    }

    public MicrosoftUserEntity save(MicrosoftUserEntity msUser) {
        return microsoftUserRepository.save(msUser);
    }

    public void delete(MicrosoftUserEntity msUserBeforeSync) {
        microsoftUserRepository.delete(msUserBeforeSync);
    }
}
