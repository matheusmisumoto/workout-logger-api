package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.Link;

public record WorkoutShortShowDTO(UUID id, 
								  UUID user,
								  OffsetDateTime date, 
								  String name, 
								  String comment, 
								  int duration,
								  List<String> target,
								  int totalLifted,
								  int totalExercises,
								  List<Link> links) {

}
