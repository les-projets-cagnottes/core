package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByEnabled(Boolean aTrue);
}