package fr.lesprojetscagnottes.core.organization.repository;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    @EntityGraph(value = "Organization.withLinkedEntities")
    List<OrganizationEntity> findAll();

    Page<OrganizationEntity> findAll(Pageable pageable);

    Set<OrganizationEntity> findAllByIdIn(Set<Long> id);

    @EntityGraph(value = "Organization.withLinkedEntities")
    Optional<OrganizationEntity> findById(Long id);

    Set<OrganizationEntity> findAllByProjects_Id(Long projectId);

    Set<OrganizationEntity> findAllByCampaigns_Id(Long campaignId);

    Set<OrganizationEntity> findAllByMembers_Id(Long userId);

    Page<OrganizationEntity> findAllByMembers_Id(long userLoggedInId, Pageable pageable);

    @EntityGraph(value = "Organization.withLinkedEntities")
    OrganizationEntity findByIdAndMembers_Id(Long id, Long userId);

    Set<OrganizationEntity> findAllByIdInAndMembers_Id(Set<Long> id, Long userId);

    Set<OrganizationEntity> findAllByContents_Id(Long contentId);

    OrganizationEntity findByNews_Id(Long id);

}
