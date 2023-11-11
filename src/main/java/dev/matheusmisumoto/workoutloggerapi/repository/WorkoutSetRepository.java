package dev.matheusmisumoto.workoutloggerapi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.model.WorkoutSet;
import jakarta.transaction.Transactional;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {
	
	@Query("SELECT DISTINCT s.exercise from WorkoutSet s WHERE s.workout=?1 ORDER BY s.exerciseOrder ASC")
	List<Exercise> findExercisesFromWorkout(Workout workout);
	
	List<WorkoutSet> findByWorkoutAndExerciseOrderBySetOrderAsc(Workout workout, Exercise exercise);
	
	@Transactional
	void deleteByWorkout(Workout workout);

}
