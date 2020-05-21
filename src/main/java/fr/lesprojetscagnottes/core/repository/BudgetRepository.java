package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Set<Budget> findAllByIdIn(Set<Long> id);

    Set<Budget> findAllByOrganizationId(Long aLong);

    @Query(nativeQuery =true,value = "SELECT * FROM budgets AS b WHERE b.start_date < :today AND b.end_date > :today AND b.is_distributed = true AND b.organization_id IN (:organizations)")
    Set<Budget> findAllUsableBudgetsInOrganizations(@Param("today") Date today, @Param("organizations") Set<Long> organizations);

    @Query(nativeQuery = true,
            value= "select b.* from budgets b " +
                    "    inner join organizations o on b.organization_id = o.id " +
                    "    inner join organizations_users on organizations_users.organization_id = o.id " +
                    "    inner join users u on u.id = organizations_users.user_id " +
                    "    where u.id = :user_id")
    Set<Budget> findAllByUser(@Param("user_id") Long userId);

    @Query(nativeQuery = true,
            value= "select b.* from budgets b " +
                    "    inner join organizations o on b.organization_id = o.id " +
                    "    inner join organizations_users on organizations_users.organization_id = o.id " +
                    "    inner join users u on u.id = organizations_users.user_id " +
                    "    where u.id = :user_id and b.id = :budget_id")
    Set<Budget> findAllByUserAndId(@Param("user_id") Long userId, @Param("budget_id") Long budgetId);

}