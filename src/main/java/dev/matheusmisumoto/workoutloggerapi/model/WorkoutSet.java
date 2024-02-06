package dev.matheusmisumoto.workoutloggerapi.model;

import java.io.Serializable;
import java.util.UUID;

import dev.matheusmisumoto.workoutloggerapi.type.WorkoutSetType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "workouts_sets")
public class WorkoutSet implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn
	private Workout workout;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn
	private Exercise exercise;
	
	@Min(1)
	private int exerciseOrder;
	
	@Min(1)
	@Max(50)
	private int setOrder;
	
	@Enumerated(EnumType.STRING)
	private WorkoutSetType type;
	
	@Max(999)
	private double weight;
	
	@Min(1)
	@Max(99)
	private int reps;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Workout getWorkout() {
		return workout;
	}

	public void setWorkout(Workout workout) {
		this.workout = workout;
	}

	public Exercise getExercise() {
		return exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public int getExerciseOrder() {
		return exerciseOrder;
	}

	public void setExerciseOrder(int exerciseOrder) {
		this.exerciseOrder = exerciseOrder;
	}

	public int getSetOrder() {
		return setOrder;
	}

	public void setSetOrder(int setOrder) {
		this.setOrder = setOrder;
	}

	public String getType() {
		return type.toString();
	}

	public void setType(WorkoutSetType type) {
		this.type = type;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getReps() {
		return reps;
	}

	public void setReps(int reps) {
		this.reps = reps;
	}
	

	
}
