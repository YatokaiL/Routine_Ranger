package ru.prilepskij.routinerangerbot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.prilepskij.routinerangerbot.entity.DialogState;
import ru.prilepskij.routinerangerbot.entity.Habit;
import ru.prilepskij.routinerangerbot.entity.RemindDays;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.service.HabitService;
import ru.prilepskij.routinerangerbot.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

        if (update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }
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

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userService.findOrCreateUser(chatId, callbackQuery.getFrom().getUserName());

        try {
            if (callbackData.startsWith("done_")) {
                Long habitId = Long.parseLong(callbackData.substring(5));

                Habit habit = habitService.findHabitById(habitId)
                        .orElseThrow(() -> new RuntimeException("Привычка не найдена"));

                habitService.markHabitAsDone(habit, LocalDate.now());

                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(chatId.toString());
                editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessage.setText("✅ Привычка '" + habit.getName() + "' выполнена!");

                execute(editMessage);
            }
            else if (callbackData.startsWith("days_")) {
                String daysType = callbackData.substring(5); // "DAILY", "WORKDAYS", "WEEKENDS"
                RemindDays remindDays = RemindDays.valueOf(daysType);

                completeHabitCreation(user, remindDays);

                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(chatId.toString());
                editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessage.setText("✅ Привычка создана! Напоминания: " + getDaysDescription(remindDays));

                execute(editMessage);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCommands(User user, String text) {
        if ("/start".equals(text)) {
            handleStartCommand(user);
        } else if (text.startsWith("/newhabit")) {
            handleNewHabitStart(user, text);
        } else if (text.startsWith("/done")){
            handleDoneHabit(user, text);
        }
        else {
            sendMessage(user.getChatId(), "Неизвестная команда. Используйте /start для начала работы.");
        }
    }

    private void handleState(User user, String text) {
        switch (user.getCurrentState()) {
            case DialogState.AWAITING_HABIT_DESCRIPTION:
                handleDescriptionInput(user, text);
                break;
            case DialogState.AWAITING_TIME:
                handleTimeInput(user, text);
                break;
            case AWAITING_DAYS:  
                handleDaysInput(user, text); 
                break;
            default:
                user.setCurrentState(DialogState.IDLE);
                userService.save(user);
        }
    }

    private void handleDaysInput(User user, String text) {
        if ("/cancel".equals(text)) {
            user.setCurrentState(DialogState.IDLE);
            user.setTempHabitName(null);
            user.setTempDescription(null);
            user.setTempReminderTime(null);
            userService.save(user);
            sendMessage(user.getChatId(), "Создание привычки отменено");
        } else {
            sendMessage(user.getChatId(), "Пожалуйста, используйте кнопки для выбора дней или /cancel для отмены");
            sendDaysSelectionKeyboard(user.getChatId());
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

    private void handleDoneHabit(User user,  String text) {
        int firstSpace = text.indexOf(' ');
        if (firstSpace > 0) {
            String habitName = text.substring(firstSpace).trim();
            List<Habit> habits = habitService.getUserHabitsByHabitName(user, habitName);

            if (habits.isEmpty()) {
                sendMessage(user.getChatId(), "Привычка не найдена 😔");
            } else if (habits.size() == 1) {
                habitService.markHabitAsDone(habits.get(0), LocalDate.now());
                sendMessage(user.getChatId(), "✅ Привычка '" + habits.get(0).getName() + "' выполнена!");
            } else {
                sendHabitSelectionButtons(user, habits);
            }

        }
        else {
            sendMessage(user.getChatId(), "Пожалуйста, укажите название привычки: /done [название]");
        }


    }

    private void sendHabitSelectionButtons(User user, List<Habit> habits) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText("\"Выберите привычку для отметки:");


        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        for (Habit habit : habits) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(habit.getName());
            button.setCallbackData("done_" + habit.getId()); // уникальный идентификатор

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        keyboardMarkup.setKeyboard(rows);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleDescriptionInput(User user, String text) {
        if ("/skip".equals(text)) {
            user.setTempDescription(null);
        } else {
            user.setTempDescription(text);
        }

        user.setCurrentState(DialogState.AWAITING_TIME);
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

        user.setTempReminderTime(reminderTime);
        user.setCurrentState(DialogState.AWAITING_DAYS);
        userService.save(user);

        sendDaysSelectionKeyboard(user.getChatId());
    }

    private void sendDaysSelectionKeyboard(Long chatId) {
        String messageText = """
        📅 Выберите дни напоминания:
        
        🌅 Ежедневно - каждый день в одно время
        🏢 По будням - с понедельника по пятницу  
        🏖️ По выходным - суббота и воскресенье
        """;

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(messageText);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton dailyBtn = new InlineKeyboardButton();
        dailyBtn.setText("🌅 Ежедневно");
        dailyBtn.setCallbackData("days_DAILY");

        InlineKeyboardButton workdaysBtn = new InlineKeyboardButton();
        workdaysBtn.setText("🏢 По будням");
        workdaysBtn.setCallbackData("days_WORKDAYS");

        InlineKeyboardButton weekendsBtn = new InlineKeyboardButton();
        weekendsBtn.setText("🏖️ По выходным");
        weekendsBtn.setCallbackData("days_WEEKENDS");

        rows.add(List.of(dailyBtn));
        rows.add(List.of(workdaysBtn));
        rows.add(List.of(weekendsBtn));

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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

    private void completeHabitCreation(User user, RemindDays remindDays) {
        Habit habit = habitService.createHabit(
                user,
                user.getTempHabitName(),
                user.getTempDescription(),
                user.getTempReminderTime(),
                remindDays,
                true
        );

        user.setCurrentState(DialogState.IDLE);
        user.setTempHabitName(null);
        user.setTempDescription(null);
        user.setTempReminderTime(null);
        userService.save(user);
    }

    private String getDaysDescription(RemindDays remindDays) {
        return switch(remindDays) {
            case DAILY -> "каждый день";
            case WORKDAYS -> "по будням";
            case WEEKENDS -> "по выходным";
            case CUSTOM -> "выбранные дни";
            default -> "не указано";
        };
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