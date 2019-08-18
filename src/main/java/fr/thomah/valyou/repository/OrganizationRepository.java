package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Page<Organization> findAll(Pageable pageable);
    Optional<Organization> findById(Long id);
    Set<Organization> findByMembers_Id(Long userId);
}