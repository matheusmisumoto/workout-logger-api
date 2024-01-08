package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record WorkoutRecordDTO(String name, String comment, @NotNull int duration, @NotBlank String status, @NotEmpty List<WorkoutExerciseRecordDTO> exercises) {

}
