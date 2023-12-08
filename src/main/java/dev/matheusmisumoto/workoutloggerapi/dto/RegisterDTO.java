package dev.matheusmisumoto.workoutloggerapi.dto;

import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;
import jakarta.validation.constraints.NotEmpty;

public record RegisterDTO(@NotEmpty String login,
						  @NotEmpty String password,
						  @NotEmpty String name,
						  UserRoleType role) {

}
