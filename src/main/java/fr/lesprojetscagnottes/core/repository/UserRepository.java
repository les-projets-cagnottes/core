package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    long count();

    @EntityGraph(value = "User.withAuthorities")
    Optional<User> findById(Long id);

    Page<User> findAll(Pageable pageable);

    User findByUsername(String username);

    @EntityGraph(value = "User.withAuthorities")
    User findByEmail(String email);

    User findBySlackUsers_Id(Long slackUserId);

    Set<User> findAllByOrganizations_id(Long id);

    Page<User> findAllByOrganizations_id(long id, Pageable pageable);

}