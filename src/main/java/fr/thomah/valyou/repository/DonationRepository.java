package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findAll(Pageable pageable);
}