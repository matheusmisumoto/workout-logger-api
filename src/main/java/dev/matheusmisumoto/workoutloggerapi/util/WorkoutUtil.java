package dev.matheusmisumoto.workoutloggerapi.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutExerciseRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutExerciseShowDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutSetRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutSetShowDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutShortShowDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.model.WorkoutSet;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutSetRepository;
import dev.matheusmisumoto.workoutloggerapi.type.WorkoutSetType;

public class WorkoutUtil {
	
	public void recordSets(ExerciseRepository exerciseRepository, 
						   WorkoutSetRepository workoutSetRepository,
						   Workout workout,
						   WorkoutRecordDTO workoutRecordDTO) {
		if(!workoutRecordDTO.exercises().isEmpty()) {
			// The order of the exercises will be based on the order given by JSON			
			int exerciseIndex = 1;
			for (WorkoutExerciseRecordDTO exercise : workoutRecordDTO.exercises()) {
				// Get the exercise object based on the ID
				var exerciseId = exerciseRepository.findById(exercise.id()).get();
				// The order of the sets will be based on the order given by JSON
				int setIndex = 1;
				for (WorkoutSetRecordDTO set : exercise.sets()) {
					var workoutSet = new WorkoutSet();
					workoutSet.setWorkout(workout);
					workoutSet.setExercise(exerciseId);
					workoutSet.setExerciseOrder(exerciseIndex);
					workoutSet.setSetOrder(setIndex++);
					workoutSet.setType(WorkoutSetType.valueOfDescription(set.type()));
					BeanUtils.copyProperties(set, workoutSet);
					workoutSetRepository.save(workoutSet);
				}
				exerciseIndex++;
			}		
		}
	}
	
	public WorkoutShowDTO buildWorkoutJSON(Optional<Workout> workout, WorkoutSetRepository workoutSetRepository) {
		// Get the training metadata
		var workoutData = workout.get();
					
		// Get the list of exercises done, already considering the order
		var exercisesData = workoutSetRepository.findExercisesFromWorkout(workoutData);
		var totalLifted = workoutSetRepository.calculateTotalWeightLifted(workoutData);
		var totalLiftedRounded = 0;
		if(totalLifted != null) { 
			totalLiftedRounded = (int) Math.round(totalLifted);
		}
				
		// From that, get the details of the exercise, and the sets data considering the order
		// Attach the list of sets on the exercise 
		List<WorkoutExerciseShowDTO> exercises = exercisesData.stream()
				.map(exercise -> {
					var setsData = workoutSetRepository.findByWorkoutAndExerciseOrderBySetOrderAsc(workout.get(), exercise);
					List<WorkoutSetShowDTO> sets = setsData.stream()
							.map(set -> {
								WorkoutSetShowDTO setDTO = new WorkoutSetShowDTO(
										set.getType(),
										set.getWeight(),
										set.getReps()
										);
								return setDTO;
							}).collect(Collectors.toList());
			
					WorkoutExerciseShowDTO exerciseDTO = new WorkoutExerciseShowDTO(
							exercise.getId(),
							exercise.getName(),
							exercise.getTarget(),
							exercise.getEquipment(),
							sets
							);
					return exerciseDTO;
					}
				).collect(Collectors.toList());;
		
		// Attach the list of exercises on the workout that will be returned as JSON
		return new WorkoutShowDTO(
					workoutData.getId(),
					workoutData.getUser().getId(),
					workoutData.getDate(),
					workoutData.getName(),
					workoutData.getComment(),
					workoutData.getDuration(),
					totalLiftedRounded,
					workoutData.getStatus(),
					exercises
				);
	}
	
	public WorkoutShortShowDTO buildWorkoutCardJSON(Workout workout, WorkoutSetRepository workoutSetRepository) {
					
		// Get the total of exercises and weight lifted
		var totalLifted = workoutSetRepository.calculateTotalWeightLifted(workout);
		var totalLiftedRounded = 0;
		if(totalLifted != null) { 
			totalLiftedRounded = (int) Math.round(totalLifted);
		}
		
		var totalExercises = workoutSetRepository.calculateTotalExercises(workout);
							
		return new WorkoutShortShowDTO(
					workout.getId(),
					workout.getUser().getId(),
					workout.getDate(),
					workout.getName(),
					workout.getComment(),
					workout.getDuration(),
					totalLiftedRounded,
					totalExercises
				);
	}
}
