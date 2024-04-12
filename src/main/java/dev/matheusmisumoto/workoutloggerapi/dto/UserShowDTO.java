package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.Link;

public record UserShowDTO(UUID id,
						  String name,
						  String login,
						  int oauthId,
						  String avatarUrl,
						  LocalDateTime joinedAt,
						  int totalWorkouts,
						  int totalLifted,
						  List<Link> links) {

}