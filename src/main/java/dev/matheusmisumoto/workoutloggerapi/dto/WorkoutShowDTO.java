package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dev.matheusmisumoto.workoutloggerapi.constants.WorkoutStatusType;

public record WorkoutShowDTO(UUID id, 
							 LocalDateTime date, 
							 String name, 
							 String comment, 
							 int duration, 
							 WorkoutStatusType status,
							 List<WorkoutExerciseShowDTO> exercises) {

}
