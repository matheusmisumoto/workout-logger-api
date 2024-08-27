package dev.matheusmisumoto.workoutloggerapi.dto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.Link;
import org.springframework.security.core.GrantedAuthority;

import dev.matheusmisumoto.workoutloggerapi.type.OAuthProviderType;

public record UserListDTO(UUID id,
						  String name,
						  String login,
						  int oauthId,
						  OAuthProviderType oauthProvider,
						  String avatarUrl,
						  LocalDateTime joinedAt,
						  Collection<? extends GrantedAuthority> authorities,
						  List<Link> links) {

}
