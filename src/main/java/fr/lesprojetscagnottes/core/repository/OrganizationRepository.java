package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @EntityGraph(value = "Organization.withLinkedEntities")
    List<Organization> findAll();

    Page<Organization> findAll(Pageable pageable);

    Set<Organization> findAllById(Set<Long> id);

    @EntityGraph(value = "Organization.withLinkedEntities")
    Optional<Organization> findById(Long id);

    Set<Organization> findAllByCampaigns_Id(Long campaignId);

    Set<Organization> findAllByMembers_Id(Long userId);

    Page<Organization> findAllByMembers_Id(long userLoggedInId, Pageable pageable);

    @EntityGraph(value = "Organization.withLinkedEntities")
    Organization findByIdAndMembers_Id(Long id, Long userId);

    Set<Organization> findAllByIdAndMembers_Id(Set<Long> id, Long userId);

    Set<Organization> findAllByContents_Id(Long contentId);
}
