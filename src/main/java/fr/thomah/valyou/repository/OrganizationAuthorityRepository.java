package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.OrganizationAuthority;
import fr.thomah.valyou.model.OrganizationAuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface OrganizationAuthorityRepository extends JpaRepository<OrganizationAuthority, Long> {
    Set<OrganizationAuthority> findAllByUsers_Id(Long id);
    Set<OrganizationAuthority> findAllByOrganizationId(Long id);
    Set<OrganizationAuthority> findByOrganizationIdAndUsersId(Long organizationId, Long userId);
    OrganizationAuthority findByOrganizationIdAndName(Long id, OrganizationAuthorityName authorityName);
    OrganizationAuthority findByOrganizationIdAndUsersIdAndName(Long organizationId, Long userId, OrganizationAuthorityName authorityName);
}