package dev.matheusmisumoto.workoutloggerapi.util;

import org.springframework.beans.BeanUtils;

import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutExerciseRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutSetRecordDTO;
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

}
