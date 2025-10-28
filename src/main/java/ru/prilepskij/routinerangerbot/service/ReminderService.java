package ru.prilepskij.routinerangerbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.repository.HabitRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final HabitRepository habitRepository;
    private final HabitService habitService;
    private final MessageSenderService messageSenderService;

    @Scheduled(cron = "0 * * * * *")
    public void checkReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // текущее время без секунд
        LocalDate today = LocalDate.now();

        log.info("🔔 Проверка напоминаний. Время: {}", now);

        List<Habit> activeHabits = habitRepository.findByIsActiveTrue();

        for (Habit habit : activeHabits) {
            if (shouldRemindToday(habit) &&
                    habit.getReminderTime() != null &&
                    habit.getReminderTime().equals(now) &&
                    !isAlreadyDoneToday(habit, today)) {

                sendReminder(habit);
            }
        }
    }

    private boolean shouldRemindToday(Habit habit) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return switch(habit.getRemindDays()) {
            case DAILY -> true;
            case WORKDAYS -> today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
            case WEEKENDS -> today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
            case CUSTOM -> false;
        };
    }

    private boolean isAlreadyDoneToday(Habit habit, LocalDate today) {
        return habitService.getHabitEntryForDate(habit, today).isPresent();
    }

    private void sendReminder(Habit habit) {
        String message = "🔔 Напоминание!\n" +
                "Не забудь: " + habit.getName() + "\n" +
                (habit.getDescription() != null ? "📝 " + habit.getDescription() + "\n" : "") +
                "\nВыполнил? Используй: /done " + habit.getName();

        try {
            messageSenderService.sendMessage(habit.getUser().getChatId(), message);
            log.info("📤 Отправлено напоминание для привычки: {}", habit.getName());
        } catch (Exception e) {
            log.error("❌ Ошибка отправки напоминания: {}", e.getMessage());
        }
    }
}