package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record WorkoutExerciseRecordDTO(UUID id, @NotEmpty List<WorkoutSetRecordDTO> sets) {

}
