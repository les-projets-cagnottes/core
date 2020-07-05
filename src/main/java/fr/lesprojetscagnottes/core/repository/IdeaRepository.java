package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRepository extends JpaRepository<Idea, Long> {


}