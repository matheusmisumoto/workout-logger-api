package dev.matheusmisumoto.workoutloggerapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.matheusmisumoto.workoutloggerapi.model.Workout;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

}
