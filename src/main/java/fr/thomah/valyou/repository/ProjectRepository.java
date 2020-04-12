package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.Project;
import fr.thomah.valyou.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findAll(Pageable pageable);

    Page<Project> findAllByStatusInOrderByStatusAscFundingDeadlineAsc(Set<ProjectStatus> status, Pageable pageable);

    Set<Project> findAllByLeaderId(Long memberId);

    Set<Project> findAllByPeopleGivingTime_Id(Long memberId);

    Set<Project> findAllByStatusAndFundingDeadlineLessThan(ProjectStatus inProgress, Date date);

    Page<Project> findByBudgets_idOrderByIdDesc(Long id, Pageable pageable);

    Set<Project> findAllByStatus(ProjectStatus projectStatus);

    Set<Project> findAllByBudgets_Id(Long id);

    @Query(nativeQuery = true, value = "select p.* from projects p " +
            "inner join project_organizations on p.id = project_organizations.project_id " +
            "inner join organizations o on project_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and p.id = :project_id")
    Set<Project> findAllProjectsByUserInOrganizations(@Param("user_id") Long userId, @Param("project_id") long projectId);
}