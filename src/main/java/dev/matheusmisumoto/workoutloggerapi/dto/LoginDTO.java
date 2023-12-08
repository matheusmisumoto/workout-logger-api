package dev.matheusmisumoto.workoutloggerapi.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginDTO(@NotEmpty String login,
					   @NotEmpty String password) {

}
