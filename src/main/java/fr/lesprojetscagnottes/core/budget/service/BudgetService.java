package fr.lesprojetscagnottes.core.budget.service;

import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public BudgetEntity findById(Long id) {
        return budgetRepository.findById(id).orElse(null);
    }

}
