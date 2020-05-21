package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Authority;
import fr.lesprojetscagnottes.core.entity.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
    Set<Authority> findAllByUsers_Id(Long id);
    Authority findByNameAndUsers_Id(AuthorityName roleAdmin, Long userId);
}