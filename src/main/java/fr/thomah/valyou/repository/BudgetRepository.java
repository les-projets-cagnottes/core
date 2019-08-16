package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Page<Budget> findAll(Pageable pageable);
    List<Budget> findAllByOrganizationId(Long aLong);
    List<Budget> findAllByEndDateGreaterThan(Date date);
    List<Budget> findAllByEndDateLessThan(Date date);

}