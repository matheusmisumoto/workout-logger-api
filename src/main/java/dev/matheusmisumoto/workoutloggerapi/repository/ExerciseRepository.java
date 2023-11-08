package dev.matheusmisumoto.workoutloggerapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.matheusmisumoto.workoutloggerapi.constants.ExerciseEquipmentType;
import dev.matheusmisumoto.workoutloggerapi.constants.ExerciseTargetType;
import dev.matheusmisumoto.workoutloggerapi.model.Exercise;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

	List<Exercise> findByTarget(ExerciseTargetType target);
	List<Exercise> findByEquipment(ExerciseEquipmentType equipment);

}
