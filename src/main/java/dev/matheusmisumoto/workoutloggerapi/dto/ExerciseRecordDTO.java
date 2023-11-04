package dev.matheusmisumoto.workoutloggerapi.dto;

import jakarta.validation.constraints.NotBlank;

public record ExerciseRecordDTO(@NotBlank String name, @NotBlank String target, @NotBlank String equipment) {
	
}
