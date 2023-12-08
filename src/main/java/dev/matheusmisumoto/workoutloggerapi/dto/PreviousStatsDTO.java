package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.List;
import java.util.UUID;

public record PreviousStatsDTO(UUID id,
							   String exercise,
		 					   List<PreviousStatsWorkoutsDTO> workouts) {

}
