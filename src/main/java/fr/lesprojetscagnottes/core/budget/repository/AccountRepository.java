package fr.lesprojetscagnottes.core.budget.repository;

import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findByOwnerIdAndBudgetId(Long ownerId, Long budgetId);

    Set<AccountEntity> findAllByOwnerId(Long ownerId);

    Page<AccountEntity> findByBudgetId(Long id, Pageable pageable);

    Set<AccountEntity> findAllByBudgetId(Long id);
}