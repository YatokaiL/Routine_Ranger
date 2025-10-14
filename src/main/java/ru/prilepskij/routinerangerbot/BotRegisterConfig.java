package ru.prilepskij.routinerangerbot;


import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotRegisterConfig {

    private final TelegramLongPollingBot bot;

    public BotRegisterConfig(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

        } catch (TelegramApiException e) {

            e.printStackTrace();
        }
    }
}
