package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByOwnerIdAndBudgetId(Long ownerId, Long budgetId);
}