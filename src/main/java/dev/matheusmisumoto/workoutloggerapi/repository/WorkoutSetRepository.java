package dev.matheusmisumoto.workoutloggerapi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.model.WorkoutSet;
import dev.matheusmisumoto.workoutloggerapi.type.ExerciseTargetType;
import jakarta.transaction.Transactional;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {
	
	@Query("SELECT DISTINCT s.exercise FROM WorkoutSet s WHERE s.workout = ?1 ORDER BY s.exerciseOrder ASC")
	List<Exercise> findExercisesFromWorkout(Workout workout);
	
	List<WorkoutSet> findByWorkoutAndExerciseOrderBySetOrderAsc(Workout workout, Exercise exercise);
	
	@Query("SELECT SUM(s.weight * s.reps) FROM WorkoutSet s WHERE s.workout = ?1")
	Double calculateTotalWeightLifted(Workout workout);
	
	@Query("SELECT COUNT(DISTINCT s.exercise) FROM WorkoutSet s WHERE s.workout = ?1")
	int calculateTotalExercises(Workout workout);
	
	@Query(value = "SELECT DISTINCT e.target FROM exercises e INNER JOIN workouts_sets s ON s.exercise_id = e.id AND s.workout_id = ?1", nativeQuery = true)
	List<ExerciseTargetType> listTargetMuscles(UUID workoutId);
	
	@Transactional
	void deleteByWorkout(Workout workout);

}
