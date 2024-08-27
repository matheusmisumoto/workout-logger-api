package dev.matheusmisumoto.workoutloggerapi.dto;

import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;

public record RegisterEditDTO(String name,
							  String password,
							  UserRoleType role) {

}
