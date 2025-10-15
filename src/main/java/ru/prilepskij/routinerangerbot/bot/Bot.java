package ru.prilepskij.routinerangerbot.bot;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.service.UserService;


public class Bot extends TelegramLongPollingBot {

    private final UserService userService;


    public Bot(String token,  UserService userService) {
        super(token);
        this.userService = userService;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.getMessage() != null && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            String text = update.getMessage().getText();

            if ("/start".equals(text)) {
                handleStartCommand(chatId, username);
            }
        }

    }

    private void handleStartCommand(Long chatId, String username) {

        User user = userService.findOrCreateUser(chatId, username);


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

        sendMessage(chatId, welcomeMessage);
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
