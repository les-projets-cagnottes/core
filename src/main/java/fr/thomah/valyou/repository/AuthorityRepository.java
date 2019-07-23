package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Authority;
import fr.thomah.valyou.model.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}