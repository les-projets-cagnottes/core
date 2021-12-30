package fr.lesprojetscagnottes.core.user.repository;

import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    long count();

    Page<UserEntity> findAll(Pageable pageable);

    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);

    UserEntity findBySlackUsers_Id(Long slackUserId);

    Set<UserEntity> findAllByProjects_Id(Long id);

    Set<UserEntity> findAllByOrganizations_id(Long id);

    Page<UserEntity> findAllByOrganizations_id(long id, Pageable pageable);

    @Query(nativeQuery = true,
            value= "select u.* from users u " +
                    "    inner join accounts a on a.owner_id  = u.id " +
                    "    inner join budgets b on b.id = a.budget_id " +
                    "    where b.id = :budget_id " +
                    "    order by a.id")
    Set<UserEntity> findAllByBudgetId(@Param("budget_id") Long budgetId);

}