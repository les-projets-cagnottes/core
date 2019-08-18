package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Page<Budget> findAll(Pageable pageable);
    Set<Budget> findAllByOrganizationId(Long aLong);
    Set<Budget> findAllByEndDateGreaterThan(Date date);
    Set<Budget> findAllByEndDateLessThan(Date date);

}