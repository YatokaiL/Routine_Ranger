package ru.prilepskij.routinerangerbot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "habit_entries")
@Getter
@Setter
public class HabitEntry {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Habit habit;

    @Column(name = "complete_date")
    private LocalDate date;

    private boolean completed;
}