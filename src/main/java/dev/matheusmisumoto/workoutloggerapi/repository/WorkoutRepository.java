package dev.matheusmisumoto.workoutloggerapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
	
	Optional<Workout> findByIdAndUser(UUID id, User user);
	List<Workout> findTop10ByUserOrderByDateDesc(User user);
	List<Workout> findAllByUserOrderByDateDesc(User user);
	
	@Query(value = "SELECT b.* FROM workouts b JOIN workouts_sets c ON b.id = c.workout_id "
			+ "WHERE c.exercise_id = ?2 "
			+ "AND b.user_id = ?1 "
			+ "GROUP BY b.id "
			+ "ORDER BY b.date DESC "
			+ "LIMIT 6", nativeQuery = true)
	List<Workout> findLatestWorkoutsWithExercise(UUID userid, UUID exerciseid);

}
