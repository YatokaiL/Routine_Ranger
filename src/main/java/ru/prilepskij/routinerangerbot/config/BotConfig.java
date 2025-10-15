package ru.prilepskij.routinerangerbot.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.prilepskij.routinerangerbot.bot.Bot;
import ru.prilepskij.routinerangerbot.service.HabitService;
import ru.prilepskij.routinerangerbot.service.UserService;

@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String token;

    @Bean
    public TelegramLongPollingBot myBot(UserService userService,  HabitService habitService) {
        return new Bot(token, userService, habitService);
    }
}