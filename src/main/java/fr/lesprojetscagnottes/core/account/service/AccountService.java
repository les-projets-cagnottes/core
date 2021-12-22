package fr.lesprojetscagnottes.core.account.service;

import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.account.model.AccountModel;
import fr.lesprojetscagnottes.core.account.repository.AccountRepository;
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

}
