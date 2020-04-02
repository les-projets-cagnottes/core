package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Set<Budget> findAllByOrganizationId(Long aLong);

    @Query(value = "select coalesce(sum(amount),0) from donations where donations.budget_id = :budget_id",
            nativeQuery = true)
    float getTotalDonations(@Param("budget_id") long budgetId);

    @Query(nativeQuery =true,value = "SELECT * FROM budgets AS b WHERE b.end_date > :endDate AND b.is_distributed = true AND b.organization_id IN (:organizations)")
    Set<Budget> findAllUsableBudgetsInOrganizations(@Param("endDate") Date endDate, @Param("organizations") List<Long> organizations);

}