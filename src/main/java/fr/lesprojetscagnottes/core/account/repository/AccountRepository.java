package fr.lesprojetscagnottes.core.account.repository;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findByOwnerIdAndBudgetId(Long ownerId, Long budgetId);

    Set<AccountEntity> findAllByOwnerId(Long ownerId);

    Page<AccountEntity> findByBudgetId(Long id, Pageable pageable);

    Set<AccountEntity> findAllByBudgetId(Long id);

    Set<AccountEntity> findAllByOwnerIdAndBudgetIdIn(Long id, Set<Long> budgetIds);
}