package dev.matheusmisumoto.workoutloggerapi.dto;

public record OAuthUserDTO(String login,
						   int id,
						   String name,
						   String avatar_url) {

}
