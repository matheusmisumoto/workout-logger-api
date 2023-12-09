package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserShowDTO(UUID id,
						  String name,
						  String login,
						  int oauthId,
						  String avatarUrl,
						  LocalDateTime joinedAt,
						  int totalWorkouts,
						  int totalLifted) {

}