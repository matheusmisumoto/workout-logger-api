package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkoutShortShowDTO(UUID id, 
								  UUID user,
								  LocalDateTime date, 
								  String name, 
								  String comment, 
								  int duration, 
								  int totalLifted,
								  int totalExercises) {

}
