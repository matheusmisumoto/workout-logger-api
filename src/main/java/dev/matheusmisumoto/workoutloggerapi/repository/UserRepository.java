package dev.matheusmisumoto.workoutloggerapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import dev.matheusmisumoto.workoutloggerapi.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	
	Optional<User> findByOauthId(int oauthId);
	
	@Query(value = "SELECT COUNT(*) FROM workoutlogger.workouts w INNER JOIN workoutlogger.users u ON w.user_id = u.id WHERE w.user_id = ?1", nativeQuery = true)
	int totalWorkouts(UUID userId);
	
	@Query(value = "SELECT SUM(s.weight * s.reps) FROM workoutlogger.workouts_sets s INNER JOIN workoutlogger.workouts w ON w.id = s.workout_id WHERE w.user_id = ?1 GROUP BY w.user_id", nativeQuery = true)
	Double calculateUserTotalWeightLifted(UUID userId);
	
	UserDetails findByLogin(String login);
}
