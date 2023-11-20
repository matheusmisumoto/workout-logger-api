package dev.matheusmisumoto.workoutloggerapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.matheusmisumoto.workoutloggerapi.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	
	Optional<User> findByName(String name);
	Optional<User> findByOauthId(int oauthId);
	
	@Query(value = "SELECT COUNT(*) FROM workouts INNER JOIN users WHERE user_id = ?1", nativeQuery = true)
	int totalWorkouts(UUID userId);
	
	@Query(value = "SELECT SUM(s.weight * s.reps) FROM workouts_sets s INNER JOIN workouts w ON w.id = s.workout_id INNER JOIN users u WHERE u.id = ?1", nativeQuery = true)
	int calculateUserTotalWeightLifted(UUID userId);
}
