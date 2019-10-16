package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Budget;
import fr.thomah.valyou.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Page<Budget> findAll(Pageable pageable);
    Set<Budget> findAllByOrganizationId(Long aLong);
    Set<Budget> findAllByEndDateGreaterThan(Date date);
    Set<Budget> findAllByEndDateLessThan(Date date);

    @Query(value = "select coalesce(sum(amount),0) from donations where donations.budget_id = :budget_id",
            nativeQuery = true)
    float getTotalDonations(@Param("budget_id") long budgetId);

}