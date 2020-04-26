package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.AuthenticationResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiTokenRepository extends JpaRepository<AuthenticationResponse, Long> {
    AuthenticationResponse findByIdAndUserId(long id, Long userId);
    List<AuthenticationResponse> findAllByUserId(long userId);
}