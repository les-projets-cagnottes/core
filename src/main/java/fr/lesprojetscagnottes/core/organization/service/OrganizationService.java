package fr.lesprojetscagnottes.core.organization.service;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public OrganizationEntity findById(Long id) {
        return organizationRepository.findById(id).orElse(null);
    }

}