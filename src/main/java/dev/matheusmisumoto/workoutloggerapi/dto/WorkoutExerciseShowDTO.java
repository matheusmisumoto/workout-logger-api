package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;
import java.util.UUID;

public record WorkoutExerciseShowDTO(UUID id,
									 String name,
									 String target,
									 String equipment,
									 List<WorkoutSetShowDTO> sets) {

}
