package fr.lesprojetscagnottes.core.project.repository;

import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Page<ProjectEntity> findAll(Pageable pageable);

    Set<ProjectEntity> findAllByLeaderId(Long memberId);

    Set<ProjectEntity> findAllByPeopleGivingTime_Id(Long memberId);

    Page<ProjectEntity> findAllByStatusIn(Set<ProjectStatus> status, Pageable pageable);

    Page<ProjectEntity> findAllByOrganizationIdAndStatusIn(Long id, Set<ProjectStatus> status, Pageable pageable);

    @Query(nativeQuery = true, value = "select c.* from projects c " +
            "inner join projects_organizations on c.id = projects_organizations.project_id " +
            "inner join organizations o on projects_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and c.id = :project_id")
    Set<ProjectEntity> findByUserAndId(@Param("user_id") Long userId, @Param("project_id") long projectId);

}