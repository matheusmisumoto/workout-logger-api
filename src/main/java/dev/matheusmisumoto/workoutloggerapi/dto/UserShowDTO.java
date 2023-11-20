package dev.matheusmisumoto.workoutloggerapi.dto;

import java.util.UUID;

public record UserShowDTO(UUID id,
						  String name,
						  String login,
						  int oauthId,
						  String avatarUrl,
						  int totalWorkouts,
						  int totalLifted) {

}