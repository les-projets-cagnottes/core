package fr.lesprojetscagnottes.core.authorization.repository;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {
    AuthorityEntity findByName(AuthorityName name);
    Set<AuthorityEntity> findAllByUsers_Id(Long id);
    AuthorityEntity findByNameAndUsers_Id(AuthorityName roleAdmin, Long userId);
}