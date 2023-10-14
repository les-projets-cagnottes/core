package fr.lesprojetscagnottes.core.vote.repository;

import fr.lesprojetscagnottes.core.vote.entity.VoteEntity;
import fr.lesprojetscagnottes.core.vote.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    Long countByTypeAndProjectId(VoteType voteType, Long projectId);

    Optional<VoteEntity> findByProjectIdAndUserId(Long id, Long userLoggedInId);
}