package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

}