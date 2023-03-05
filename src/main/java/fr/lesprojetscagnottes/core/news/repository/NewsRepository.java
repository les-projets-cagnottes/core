package fr.lesprojetscagnottes.core.news.repository;

import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    Page<NewsEntity> findAllByOrganizationIdOrOrganizationIdIsNull(Long organizationId, Pageable pageable);

    Page<NewsEntity> findAllByProjectId(Long id, Pageable pageable);

    NewsEntity findFirstByProjectIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(Long id, Date createdAt);
}