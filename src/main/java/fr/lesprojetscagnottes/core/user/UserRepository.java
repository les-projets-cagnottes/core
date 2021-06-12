package fr.lesprojetscagnottes.core.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    long count();

    @EntityGraph(value = "User.withAuthorities")
    Optional<UserEntity> findById(Long id);

    Page<UserEntity> findAll(Pageable pageable);

    UserEntity findByUsername(String username);

    @EntityGraph(value = "User.withAuthorities")
    UserEntity findByEmail(String email);

    UserEntity findBySlackUsers_Id(Long slackUserId);

    Set<UserEntity> findAllByProjects_Id(Long id);

    Set<UserEntity> findAllByCampaigns_Id(Long id);

    Set<UserEntity> findAllByOrganizations_id(Long id);

    Page<UserEntity> findAllByOrganizations_id(long id, Pageable pageable);

}