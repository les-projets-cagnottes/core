package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Page<Budget> findAll(Pageable pageable);
}