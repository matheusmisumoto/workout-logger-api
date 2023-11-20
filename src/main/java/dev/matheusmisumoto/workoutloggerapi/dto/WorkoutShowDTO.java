package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dev.matheusmisumoto.workoutloggerapi.type.WorkoutStatusType;

public record WorkoutShowDTO(UUID id, 
							 UUID user,
							 LocalDateTime date, 
							 String name, 
							 String comment, 
							 int duration, 
							 int totalLifted,
							 WorkoutStatusType status,
							 List<WorkoutExerciseShowDTO> exercises) {

}
