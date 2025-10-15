package ru.prilepskij.routinerangerbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.prilepskij.routinerangerbot.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByChatId(Long chatId);  // Ищем по chatId из Telegram
}