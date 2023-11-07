package dev.matheusmisumoto.workoutloggerapi.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import dev.matheusmisumoto.workoutloggerapi.model.WorkoutStatusType;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutRepository;

@RestController
@RequestMapping("/v1/workout")
public class WorkoutController {
	
	@Autowired
	WorkoutRepository workoutRepository;
	
	@PostMapping
	public ResponseEntity<Workout> saveWorkout(@RequestBody WorkoutRecordDTO workoutRecordDTO) {
		var workout = new Workout();
		workout.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		workout.setDate(LocalDateTime.now(Clock.systemUTC()));
		BeanUtils.copyProperties(workoutRecordDTO, workout);;
		return ResponseEntity.status(HttpStatus.CREATED).body(workoutRepository.save(workout));
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
		return ResponseEntity.status(HttpStatus.OK).body(workout);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateWorkout(@PathVariable(value="id") UUID id,
											   @RequestBody WorkoutRecordDTO workoutRecordDTO){
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}
		var workoutData = workout.get();
		workoutData.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		BeanUtils.copyProperties(workoutRecordDTO, workoutData);
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
