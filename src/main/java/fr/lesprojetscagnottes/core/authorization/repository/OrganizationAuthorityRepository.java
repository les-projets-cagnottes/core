package fr.lesprojetscagnottes.core.authorization.repository;

import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface OrganizationAuthorityRepository extends JpaRepository<OrganizationAuthorityEntity, Long> {
    Set<OrganizationAuthorityEntity> findAllByUsers_Id(Long id);

    Set<OrganizationAuthorityEntity> findAllByOrganizationId(Long id);

    Set<OrganizationAuthorityEntity> findByOrganizationIdAndUsersId(Long organizationId, Long userId);

    OrganizationAuthorityEntity findByOrganizationIdAndName(Long id, OrganizationAuthorityName authorityName);

    OrganizationAuthorityEntity findByOrganizationIdAndUsersIdAndName(Long organizationId, Long userId, OrganizationAuthorityName authorityName);
}