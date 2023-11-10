package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;
import java.util.UUID;

public record WorkoutExerciseRecordDTO(UUID id, List<WorkoutSetRecordDTO> sets) {

}
