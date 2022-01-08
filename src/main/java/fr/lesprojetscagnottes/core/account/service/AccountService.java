package fr.lesprojetscagnottes.core.account.service;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.account.repository.AccountRepository;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.budget.service.BudgetService;
import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.service.OrganizationService;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    public AccountEntity getById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public Set<AccountModel> getByIds(Principal principal, Set<Long> ids) {
        Long userLoggedInId = userService.get(principal).getId();
        boolean userLoggedIn_isNotAdmin = userService.isNotAdmin(userLoggedInId);
        Set<AccountModel> models = new LinkedHashSet<>();

        for(Long id : ids) {

            // Retrieve full referenced objects
            AccountEntity entity = getById(id);
            if(entity == null) {
                log.error("Impossible to get account {} : it doesn't exist", id);
                continue;
            }

            // Verify that principal share an organization with the user
            if(userLoggedIn_isNotAdmin && !userService.isMemberOfOrganization(userLoggedInId, entity.getBudget().getOrganization().getId())) {
                log.error("Impossible to get account {} : principal {} is not in its organization", id, userLoggedInId);
                continue;
            }

            // Add the user to returned list
            models.add(AccountModel.fromEntity(entity));
        }

        return models;
    }

    public AccountEntity getByBudgetAndUser(Long budgetId, Long userId) {
        return accountRepository.findByOwnerIdAndBudgetId(userId, budgetId);
    }

    public AccountModel getByBudgetAndUser(Principal principal, Long budgetId, Long userId) {

        // Fails if budget ID is missing
        if(budgetId <= 0 || userId <= 0) {
            log.error("Impossible to get account by budget ID and user ID : params are incorrect");
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        BudgetEntity budget = budgetService.findById(budgetId);
        UserEntity user = userService.findById(userId);

        // Verify that any of references are not null
        if(budget == null || user == null) {
            log.error("Impossible to get account by budget ID and user ID : one or more reference(s) does not exist");
            throw new NotFoundException();
        }

        // Verify that principal is the organization of the budget
        Long userLoggedInId = userService.get(principal).getId();
        Set<OrganizationEntity> principalOrganizations = organizationService.findAllByMembersId(userLoggedInId);
        if(principalOrganizations.stream().noneMatch(organization -> organization.getId().equals(budget.getOrganization().getId())) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get account by budget ID and user ID : the principal is not in the budget organization");
            throw new ForbiddenException();
        }

        // Verify that principal is in the same organization as the user requested
        Set<OrganizationEntity> userOrganizations = organizationService.findAllByMembersId(userId);
        if(userService.hasNoACommonOrganization(principalOrganizations, userOrganizations) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to get account by budget ID and user ID : user {} is not in the same organization as the principal", userId);
            throw new ForbiddenException();
        }

        return AccountModel.fromEntity(getByBudgetAndUser(budgetId, userId));
    }

    public AccountEntity save(AccountEntity account) {
        return accountRepository.save(account);
    }

}
