package fr.lesprojetscagnottes.core.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiTokenRepository extends JpaRepository<AuthenticationResponseEntity, Long> {
    AuthenticationResponseEntity findByIdAndUserId(long id, Long userId);
    List<AuthenticationResponseEntity> findAllByUserId(long userId);
    List<AuthenticationResponseEntity> findAllByDescription(String description);
}