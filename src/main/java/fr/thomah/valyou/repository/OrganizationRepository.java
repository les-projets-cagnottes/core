package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Page<Organization> findAll(Pageable pageable);
}