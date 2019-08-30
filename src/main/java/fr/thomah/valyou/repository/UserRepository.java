package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    long count();
    Page<User> findAll(Pageable pageable);
    User findByEmail(String email);
    Page<User> findByOrganizations_idOrderByIdAsc(long id, Pageable pageable);
}