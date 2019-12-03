package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Project;
import fr.thomah.valyou.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAll(Pageable pageable);
    Page<Project> findAllByStatusInOrderByStatusDescFundingDeadlineAsc(Set<ProjectStatus> status, Pageable pageable);
    Set<Project> findAllByLeaderId(Long memberId);
    Set<Project> findAllByPeopleGivingTime_Id(Long memberId);
    Set<Project> findAllByStatusAndFundingDeadlineLessThan(ProjectStatus inProgress, Date date);
    Page<Project> findByBudgets_idOrderByIdDesc(Long id, Pageable pageable);
}