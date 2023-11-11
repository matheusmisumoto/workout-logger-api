package dev.matheusmisumoto.workoutloggerapi.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutExerciseShowDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutSetShowDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutSetRepository;
import dev.matheusmisumoto.workoutloggerapi.type.WorkoutStatusType;
import dev.matheusmisumoto.workoutloggerapi.util.WorkoutUtil;

@RestController
@RequestMapping("/v1/workout")
public class WorkoutController {
	
	@Autowired
	WorkoutRepository workoutRepository;
	
	@Autowired
	ExerciseRepository exerciseRepository;
	
	@Autowired
	WorkoutSetRepository workoutSetRepository;
	
	@PostMapping
	public ResponseEntity<Object> saveWorkout(@RequestBody WorkoutRecordDTO workoutRecordDTO) {
		// Save metadata
		var workout = new Workout();
		workout.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		workout.setDate(LocalDateTime.now(Clock.systemUTC()));
		BeanUtils.copyProperties(workoutRecordDTO, workout);
		var workoutMetadata = workoutRepository.save(workout);
		
		var sets = new WorkoutUtil();
		sets.recordSets(exerciseRepository, workoutSetRepository, workoutMetadata, workoutRecordDTO);
				
		return ResponseEntity.status(HttpStatus.CREATED).body(workoutMetadata);
	}
	
	@GetMapping
	public ResponseEntity<List<Workout>> allWorkouts() {
		return ResponseEntity.status(HttpStatus.OK).body(workoutRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getWorkout(@PathVariable(value="id") UUID id) {
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		
		// Get the training metadata
		var workoutData = workout.get();
		
		// Get the list of exercises done, already considering the order
		var exercisesData = workoutSetRepository.findExercisesFromWorkout(workoutData);
		
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
		WorkoutShowDTO response = new WorkoutShowDTO(
					workoutData.getId(),
					workoutData.getDate(),
					workoutData.getName(),
					workoutData.getComment(),
					workoutData.getDuration(),
					workoutData.getStatus(),
					exercises
				);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateWorkout(@PathVariable(value="id") UUID id,
											   @RequestBody WorkoutRecordDTO workoutRecordDTO){
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		
		// Prepare metadata to be updated
		var workoutData = workout.get();
		workoutData.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		BeanUtils.copyProperties(workoutRecordDTO, workoutData);
		
		// It would take unnecessary steps of code to compare the list of recorded sets
		// with the updated ones (changes, inserts and deletions).
		// So we delete all recorded sets of the training, and insert the updated ones
		workoutSetRepository.deleteByWorkout(workoutData);
		var sets = new WorkoutUtil();
		sets.recordSets(exerciseRepository, workoutSetRepository, workoutData, workoutRecordDTO);
		
		return ResponseEntity.status(HttpStatus.OK).body(workoutRepository.save(workoutData));		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteWorkout(@PathVariable(value="id") UUID id) {
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		workoutRepository.delete(workout.get());
		return ResponseEntity.status(HttpStatus.OK).body("Workout deleted");
	}
}
