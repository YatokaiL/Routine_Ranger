package ru.prilepskij.routinerangerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.HabitEntry;
import ru.prilepskij.routinerangerbot.entity.RemindDays;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.repository.HabitEntryRepository;
import ru.prilepskij.routinerangerbot.repository.HabitRepository;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository habitEntryRepository;


    public Habit createHabit(User user, String name, String description,
                             LocalTime reminderTime, RemindDays remindDays, Boolean isActive) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setDescription(description);
        habit.setUser(user);
        habit.setCreationDate(LocalDate.now());
        habit.setReminderTime(reminderTime);
        habit.setRemindDays(remindDays);
        habit.setIsActive(isActive);

        return habitRepository.save(habit);
    }

    public List<Habit> getUserHabitsByHabitName(User user, String name) {
        return habitRepository.findByUserAndNameContainingIgnoreCase(user, name);
    }

    public HabitEntry markHabitAsDone (Habit habit, LocalDate date){
        HabitEntry habitEntry = new HabitEntry();
        habitEntry.setHabit(habit);
        habitEntry.setDate(date);

        return habitEntryRepository.save(habitEntry);
    }


    public List<Habit> getUserHabits(User user) {
        return habitRepository.findByUser(user);
    }

    public Optional<Habit> findHabitById(Long habitId) {
        return habitRepository.findById(habitId);
    }
}
