package fr.lesprojetscagnottes.core.budget.repository;

import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    Set<BudgetEntity> findAllByIdIn(Set<Long> id);

    Set<BudgetEntity> findAllByCampaigns_Id(Long id);

    Set<BudgetEntity> findAllByOrganizationId(Long id);

    Set<BudgetEntity> findAllByRulesId(Long id);

    Set<BudgetEntity> findALlByEndDateGreaterThanAndIsDistributedAndAndOrganizationId(Date enDate, boolean isDistributed, Long organizationId);

    @Query(nativeQuery =true,value = "SELECT * FROM budgets AS b WHERE b.start_date < :today AND b.end_date > :today AND b.is_distributed = true AND b.organization_id IN (:organizations)")
    Set<BudgetEntity> findAllUsableBudgetsInOrganizations(@Param("today") Date today, @Param("organizations") Set<Long> organizations);

    @Query(nativeQuery = true,
            value= "select b.* from budgets b " +
                    "    inner join organizations o on b.organization_id = o.id " +
                    "    inner join organizations_users on organizations_users.organization_id = o.id " +
                    "    inner join users u on u.id = organizations_users.user_id " +
                    "    where u.id = :user_id")
    Set<BudgetEntity> findAllByUser(@Param("user_id") Long userId);

    @Query(nativeQuery = true,
            value= "select b.* from budgets b " +
                    "    inner join organizations o on b.organization_id = o.id " +
                    "    inner join organizations_users on organizations_users.organization_id = o.id " +
                    "    inner join users u on u.id = organizations_users.user_id " +
                    "    where u.id = :user_id and b.id = :budget_id")
    Set<BudgetEntity> findAllByUserAndId(@Param("user_id") Long userId, @Param("budget_id") Long budgetId);

}