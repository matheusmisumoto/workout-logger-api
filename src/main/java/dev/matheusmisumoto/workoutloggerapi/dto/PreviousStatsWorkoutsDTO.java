package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PreviousStatsWorkoutsDTO(LocalDateTime date, 
		  							   List<WorkoutSetShowDTO> sets) {

}
