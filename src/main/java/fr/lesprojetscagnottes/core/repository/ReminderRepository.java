package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findAllByEnabled(Boolean aTrue);
}