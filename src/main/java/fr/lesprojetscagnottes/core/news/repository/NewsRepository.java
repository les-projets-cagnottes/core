package fr.lesprojetscagnottes.core.news.repository;

import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    @Query(nativeQuery = true,
            value = "select c.* from projects c " +
                    "inner join projects_organizations on c.id = projects_organizations.project_id " +
                    "inner join organizations o on projects_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 and c.status IN (?2) --#pageable\n",
            countQuery = "select count(*) from projects c " +
                    "inner join projects_organizations on c.id = projects_organizations.project_id " +
                    "inner join organizations o on projects_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 c.status IN (?2)")
    Page<NewsEntity> findAllByUser(Long userId, Pageable pageable);

    Page<NewsEntity> findAllByOrganization_IdOrOrganizationIdIsNull(Long organizationId, Pageable pageable);
}