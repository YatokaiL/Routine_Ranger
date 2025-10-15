package ru.prilepskij.routinerangerbot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.prilepskij.routinerangerbot.entity.DialogState;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.service.HabitService;
import ru.prilepskij.routinerangerbot.service.UserService;

import java.time.LocalTime;

public class Bot extends TelegramLongPollingBot {

    private final UserService userService;
    private final HabitService habitService;

    public Bot(String token, UserService userService, HabitService habitService) {
        super(token);
        this.userService = userService;
        this.habitService = habitService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            String text = update.getMessage().getText();

            User user = userService.findOrCreateUser(chatId, username);

            if (user.getCurrentState() != DialogState.IDLE) {
                handleState(user, text);
            } else {
                handleCommands(user, text);
            }
        }
    }

    private void handleCommands(User user, String text) {
        if ("/start".equals(text)) {
            handleStartCommand(user);
        } else if (text.startsWith("/newhabit")) {
            handleNewHabitStart(user, text);
        } else {
            sendMessage(user.getChatId(), "Неизвестная команда. Используйте /start для начала работы.");
        }
    }

    private void handleState(User user, String text) {
        switch (user.getCurrentState()) {
            case DialogState.AWAITING_HABIT_DESCRIPTION:
                handleDescriptionInput(user, text);
                break;
            case DialogState.AWAITING_HABIT_TIME:
                handleTimeInput(user, text);
                break;
            default:
                user.setCurrentState(DialogState.IDLE);
                userService.save(user);
        }
    }

    private void handleNewHabitStart(User user, String text) {
        int firstSpace = text.indexOf(' ');
        if (firstSpace > 0) {
            String habitName = text.substring(firstSpace).trim();
            user.setTempHabitName(habitName);
            user.setCurrentState(DialogState.AWAITING_HABIT_DESCRIPTION);
            userService.save(user);

            sendMessage(user.getChatId(), "Введите описание привычки (или /skip чтобы пропустить):");
        } else {
            sendMessage(user.getChatId(), "Пожалуйста, укажите название привычки: /newhabit [название]");
        }
    }

    private void handleDescriptionInput(User user, String text) {
        if ("/skip".equals(text)) {
            user.setTempDescription(null);
        } else {
            user.setTempDescription(text);
        }

        user.setCurrentState(DialogState.AWAITING_HABIT_TIME);
        userService.save(user);
        sendMessage(user.getChatId(), "Во сколько напоминать? (формат ЧЧ:ММ или /skip):");
    }

    private void handleTimeInput(User user, String text) {
        LocalTime reminderTime = null;

        if (!"/skip".equals(text)) {
            try {
                reminderTime = LocalTime.parse(text);
            } catch (Exception e) {
                sendMessage(user.getChatId(), "Неверный формат времени. Используйте ЧЧ:ММ (например 09:30)");
                return;
            }
        }

        // Создаем привычку
        Habit habit = habitService.createHabit(
                user,
                user.getTempHabitName(),
                user.getTempDescription(),
                reminderTime
        );

        // Сбрасываем состояние и временные данные
        user.setCurrentState(DialogState.IDLE);
        user.setTempHabitName(null);
        user.setTempDescription(null);
        userService.save(user);

        String message = "Привычка '" + habit.getName() + "' успешно создана!";
        if (reminderTime != null) {
            message += "\nНапоминание установлено на: " + reminderTime;
        }
        sendMessage(user.getChatId(), message);
    }

    private void handleStartCommand(User user) {
        String welcomeMessage = """
            🌲 Добро пожаловать в Routine Ranger!
            
            Я помогу тебе отслеживать привычки и строить рутины.
            
            📋 Доступные команды:
            /newhabit [название] - добавить новую привычку
            /habits - список моих привычек
            /done [название] - отметить выполнение
            /stats - статистика
            
            Начни с добавления первой привычки! 💪
            """;

        sendMessage(user.getChatId(), welcomeMessage);
    }

    @Override
    public String getBotUsername() {
        return "RoutineRangerBot";
    }

    public void sendMessage(Long userId, String msg){
        SendMessage sm = SendMessage.builder()
                .chatId(userId.toString()).text(msg).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}