package fr.lesprojetscagnottes.core.news.repository;

import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

}