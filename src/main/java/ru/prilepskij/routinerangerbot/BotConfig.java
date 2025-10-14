package ru.prilepskij.routinerangerbot;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String token;

    @Bean
    public TelegramLongPollingBot myBot() {
        return new Bot(token);
    }
}
