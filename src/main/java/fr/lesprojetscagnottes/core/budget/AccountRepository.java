package fr.lesprojetscagnottes.core.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByOwnerIdAndBudgetId(Long ownerId, Long budgetId);

    Set<Account> findAllByOwnerId(Long ownerId);

    Page<Account> findByBudgetId(Long id, Pageable pageable);

    Set<Account> findAllByBudgetId(Long id);
}