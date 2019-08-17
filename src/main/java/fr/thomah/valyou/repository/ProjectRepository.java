package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAll(Pageable pageable);
    List<Project> findAllByLeaderId(Long memberId);
    List<Project> findAllByPeopleGivingTime_Id(Long memberId);
}