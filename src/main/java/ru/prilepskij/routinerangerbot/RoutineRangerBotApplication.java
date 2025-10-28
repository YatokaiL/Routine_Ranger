package ru.prilepskij.routinerangerbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoutineRangerBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutineRangerBotApplication.class, args);
    }

}
