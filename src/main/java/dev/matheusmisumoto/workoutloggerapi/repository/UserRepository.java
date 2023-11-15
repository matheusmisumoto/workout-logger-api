package dev.matheusmisumoto.workoutloggerapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.matheusmisumoto.workoutloggerapi.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	
	Optional<User> findByName(String name);
	Optional<User> findByOauthId(int oauthId);
	
}
