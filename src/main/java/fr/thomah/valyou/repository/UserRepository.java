package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAll(Pageable pageable);
    User findByEmail(String email);
    User findByEmailAndPassword(String email, String password);

    User findByUsername(String username);
}