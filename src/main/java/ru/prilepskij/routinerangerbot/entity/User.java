package ru.prilepskij.routinerangerbot.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_id")
    private Long chatId;

    @Column(name = "username")
    private String username;

    @Column(name = "reg_date")
    private LocalDate registrationDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Habit> habits = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DialogState currentState = DialogState.IDLE;


    private String tempHabitName;
    private String tempDescription;

    @Override
    public String toString() {
        return "User with db_id: " + id + " tg_id: " + chatId + " username: " + username + " registration date: " + registrationDate;
    }
}
