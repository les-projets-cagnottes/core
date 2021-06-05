package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Project;
import fr.lesprojetscagnottes.core.model.CampaignStatus;
import fr.lesprojetscagnottes.core.model.ProjectStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @NotNull
    Page<Project> findAll(@NotNull Pageable pageable);

    Set<Project> findAllByLeaderId(Long memberId);

    Set<Project> findAllByPeopleGivingTime_Id(Long memberId);

    Set<Project> findAllByStatus(CampaignStatus status);

    Page<Project> findAllByStatusIn(Set<CampaignStatus> status, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select c.* from projects c " +
                    "inner join projects_organizations on c.id = projects_organizations.project_id " +
                    "inner join organizations o on projects_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 and c.status IN (?2) --#pageable\n",
            countQuery = "select count(*) from projects c " +
                    "inner join projects_organizations on c.id = projects_organizations.project_id " +
                    "inner join organizations o on projects_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 c.status IN (?2)")
    Page<Project> findAllByUserAndStatus(Long userId, Set<ProjectStatus> status, Pageable pageable);
    Page<Project> findAllByOrganizations_IdAndStatusIn(Long id, Set<ProjectStatus> status, Pageable pageable);

    @Query(nativeQuery = true, value = "select c.* from projects c " +
            "inner join projects_organizations on c.id = projects_organizations.project_id " +
            "inner join organizations o on projects_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and c.id = :project_id")
    Set<Project> findByUserAndId(@Param("user_id") Long userId, @Param("project_id") long projectId);

}