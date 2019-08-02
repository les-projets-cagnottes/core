package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.AuthorityName;
import fr.thomah.valyou.model.Organization;
import fr.thomah.valyou.model.OrganizationAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationAuthorityRepository extends JpaRepository<OrganizationAuthority, Long> {
    OrganizationAuthority findByOrganizationAndName(Organization organization, AuthorityName name);
}