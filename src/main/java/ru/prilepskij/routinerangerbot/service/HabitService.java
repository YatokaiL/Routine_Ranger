package ru.prilepskij.routinerangerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.repository.HabitRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;


    public Habit createHabit(User user, String name, String description, LocalTime reminderTime) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setDescription(description);
        habit.setUser(user);
        habit.setCreationDate(LocalDate.now());
        habit.setReminderTime(reminderTime);

        return habitRepository.save(habit);
    }


    public List<Habit> getUserHabits(User user) {
        return habitRepository.findByUser(user);
    }
}
