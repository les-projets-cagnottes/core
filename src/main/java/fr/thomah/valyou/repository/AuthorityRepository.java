package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Authority;
import fr.thomah.valyou.model.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
    Set<Authority> findAllByUsers_Id(Long id);
}