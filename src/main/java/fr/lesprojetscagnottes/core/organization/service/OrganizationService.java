package fr.lesprojetscagnottes.core.organization.service;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationEntity findById(Long id) {
        return organizationRepository.findById(id).orElse(null);
    }

    public Set<OrganizationEntity> findAllByMembersId(Long userId) {
        return organizationRepository.findAllByMembers_Id(userId);
    }

    public OrganizationEntity save(OrganizationEntity organization) {
        return organizationRepository.save(organization);
    }

    public Set<OrganizationEntity> findAllByMembers_Id(Long userId) {
        return organizationRepository.findAllByMembers_Id(userId);
    }

    public Set<OrganizationEntity> findAllByProjects_Id(Long projectId) {
        return organizationRepository.findAllByProjects_Id(projectId);
    }
}
