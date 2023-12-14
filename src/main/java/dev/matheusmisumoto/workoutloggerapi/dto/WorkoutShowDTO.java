package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import dev.matheusmisumoto.workoutloggerapi.type.WorkoutStatusType;

public record WorkoutShowDTO(UUID id, 
							 UUID user,
							 OffsetDateTime date, 
							 String name, 
							 String comment, 
							 int duration, 
							 int totalLifted,
							 WorkoutStatusType status,
							 List<WorkoutExerciseShowDTO> exercises) {

}
