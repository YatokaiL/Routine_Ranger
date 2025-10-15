package ru.prilepskij.routinerangerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prilepskij.routinerangerbot.entity.User;
import ru.prilepskij.routinerangerbot.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public User findOrCreateUser(Long chatId, String username) {
        Optional<User> existingUser = userRepository.findByChatId(chatId);

        if (existingUser.isPresent()) {
            return existingUser.get();  // Вернуть существующего
        } else {

            User newUser = new User();
            newUser.setChatId(chatId);
            newUser.setUsername(username);
            newUser.setRegistrationDate(LocalDate.now());
            return userRepository.save(newUser);
        }
    }
}