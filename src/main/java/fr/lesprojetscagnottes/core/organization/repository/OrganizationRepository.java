package fr.lesprojetscagnottes.core.organization.repository;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    List<OrganizationEntity> findAll();

    Page<OrganizationEntity> findAll(Pageable pageable);

    Optional<OrganizationEntity> findById(Long id);

    Set<OrganizationEntity> findAllByProjects_Id(Long projectId);

    Set<OrganizationEntity> findAllByMembers_Id(Long userId);

    Page<OrganizationEntity> findAllByMembers_Id(long userLoggedInId, Pageable pageable);

    OrganizationEntity findByIdAndMembers_Id(Long id, Long userId);

    Set<OrganizationEntity> findAllByContents_Id(Long contentId);

    OrganizationEntity findByNews_Id(Long id);

}
