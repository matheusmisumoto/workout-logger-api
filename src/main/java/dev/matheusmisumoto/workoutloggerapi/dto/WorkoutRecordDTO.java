package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkoutRecordDTO(@NotBlank UUID user, String name, String comment, @NotNull int duration, @NotBlank String status, List<WorkoutExerciseRecordDTO> exercises) {

}
