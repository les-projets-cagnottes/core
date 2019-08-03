package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Organization;
import fr.thomah.valyou.model.OrganizationAuthority;
import fr.thomah.valyou.model.OrganizationAuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationAuthorityRepository extends JpaRepository<OrganizationAuthority, Long> {
    OrganizationAuthority findByOrganizationAndName(Organization organization, OrganizationAuthorityName name);
}