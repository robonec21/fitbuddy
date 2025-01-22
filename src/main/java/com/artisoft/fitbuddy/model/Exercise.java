package com.artisoft.fitbuddy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    // Default/suggested values
    private Integer defaultSets;
    private Integer defaultRepsPerSet;
    private Integer defaultRestPeriodBetweenSets;

    @Column(length = 500)
    private String mediaLink;

    @OneToMany(mappedBy = "exercise")
    private Set<WorkoutDayExercise> workoutDayExercises;
}
