package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    long count();

    Page<User> findAll(Pageable pageable);

    User findByEmail(String email);

    Set<User> findByOrganizations_idOrderByIdAsc(long id);

    Page<User> findByOrganizations_idOrderByIdAsc(long id, Pageable pageable);

    @Query(value = "select users.* as totalBudgetDonations from users inner join organizations_users on organizations_users.user_id = users.id left join donations on users.id = donations.contributor_id where donations.budget_id = :budget_id or donations.id is null group by users.id ORDER BY users.id",
            countQuery = "select count(*) from users inner join organizations_users on organizations_users.user_id = users.id left join donations on users.id = donations.contributor_id where donations.budget_id = :budget_id or donations.id is null group by users.id",
            nativeQuery = true)
    Page<User> findByBudgetIdWithPagination(@Param("budget_id") long budgetId, Pageable pageable);

    @Query(value = "select sum(donations.amount) as totalBudgetDonations from users inner join organizations_users on organizations_users.user_id = users.id left join donations on users.id = donations.contributor_id where donations.budget_id = :budget_id or donations.id is null group by users.id ORDER BY users.id",
            countQuery = "select count(*) from users inner join organizations_users on organizations_users.user_id = users.id left join donations on users.id = donations.contributor_id where donations.budget_id = :budget_id or donations.id is null group by users.id",
            nativeQuery = true)
    Page<Float> sumTotalBudgetDonationsByBudgetIdWithPagination(@Param("budget_id") long budgetId, Pageable pageable);
}