package ru.prilepskij.routinerangerbot.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "habits")
@Getter
@Setter
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "remind_time")
    private LocalTime reminderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "remind_type")
    private RemindType reminderDays;

    @Column(name = "creation_date")
    private LocalDate creationDate;


}
