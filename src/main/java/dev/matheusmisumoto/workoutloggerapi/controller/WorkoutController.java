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

import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
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
	
	@Autowired
	UserRepository userRepository;
	
	@PostMapping
	public ResponseEntity<Object> saveWorkout(@RequestBody WorkoutRecordDTO workoutRecordDTO) {
		// Save metadata
		var workout = new Workout();
		workout.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		workout.setDate(LocalDateTime.now(Clock.systemUTC()));
		workout.setUser(userRepository.findById(workoutRecordDTO.user()).get());
		BeanUtils.copyProperties(workoutRecordDTO, workout);
		var workoutMetadata = workoutRepository.save(workout);
		
		var sets = new WorkoutUtil();
		sets.recordSets(exerciseRepository, workoutSetRepository, workoutMetadata, workoutRecordDTO);
				
		return ResponseEntity.status(HttpStatus.CREATED).body(workoutMetadata);
	}
	
	@GetMapping("/user/{id}")
	public ResponseEntity<Object> allUserWorkouts(@PathVariable(value="id") UUID id) {
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		var allWorkouts = workoutRepository.findAllByUser(user.get());
		var responseBuilder = new WorkoutUtil();

		List<Object> response = allWorkouts.stream()
				.map(workout -> {
					return responseBuilder.buildWorkoutCardJSON(workout, workoutSetRepository);
				}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getWorkout(@PathVariable(value="id") UUID id) {
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		
		var response = new WorkoutUtil();

		return ResponseEntity.status(HttpStatus.OK).body(response.buildWorkoutJSON(workout, workoutSetRepository));
	}

	@GetMapping("/{userid}/{id}")
	public ResponseEntity<Object> getWorkout(@PathVariable(value="userid") UUID userid,
											 @PathVariable(value="id") UUID id) {
		Optional<User> user = userRepository.findById(userid);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		Optional<Workout> workout = workoutRepository.findByIdAndUser(id, user.get());
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		
		var response = new WorkoutUtil();

		return ResponseEntity.status(HttpStatus.OK).body(response.buildWorkoutJSON(workout, workoutSetRepository));
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
