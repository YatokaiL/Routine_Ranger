package ru.prilepskij.routinerangerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.User;
import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUser(User user);
    List<Habit> findByUserAndNameContainingIgnoreCase(User user, String name);
}