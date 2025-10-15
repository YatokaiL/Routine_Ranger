package ru.prilepskij.routinerangerbot.bot;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class Bot extends TelegramLongPollingBot {


    public Bot(String token) {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {

        var msg = update.getMessage();
        var user = update.getMessage().getFrom();
        var userId = user.getId();

        System.out.println(user.getFirstName() + " with ID:" + userId + " wrote " + msg.getText());

        if (update.hasMessage() && update.getMessage().hasText()) {
            sendMessage(userId, "Ты написал " + msg.getText());
        }

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
