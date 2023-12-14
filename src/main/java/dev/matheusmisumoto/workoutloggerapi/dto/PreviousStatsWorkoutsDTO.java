package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record PreviousStatsWorkoutsDTO(OffsetDateTime date, 
		  							   List<WorkoutSetShowDTO> sets) {

}
