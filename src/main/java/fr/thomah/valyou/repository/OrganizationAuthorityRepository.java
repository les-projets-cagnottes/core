package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.OrganizationAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface OrganizationAuthorityRepository extends JpaRepository<OrganizationAuthority, Long> {
    Set<OrganizationAuthority> findAllByUsers_Id(Long id);
    Set<OrganizationAuthority> findAllByOrganizationId(Long id);
}