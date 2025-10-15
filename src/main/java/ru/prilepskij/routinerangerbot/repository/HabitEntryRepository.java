package ru.prilepskij.routinerangerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.HabitEntry;
import java.time.LocalDate;
import java.util.Optional;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {
    Optional<HabitEntry> findByHabitAndDate(Habit habit, LocalDate date);
}