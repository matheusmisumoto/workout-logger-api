package dev.matheusmisumoto.workoutloggerapi.controller;

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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


import dev.matheusmisumoto.workoutloggerapi.dto.ExerciseRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.model.ExerciseEquipmentType;
import dev.matheusmisumoto.workoutloggerapi.model.ExerciseTargetType;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/exercises")
public class ExerciseController {
	
	@Autowired
	ExerciseRepository exerciseRepository;
	
	@PostMapping
	public ResponseEntity<Exercise> saveExercise(@RequestBody @Valid ExerciseRecordDTO exerciseRecordDTO) {
		var exercise = new Exercise();
		exercise.setTarget(ExerciseTargetType.valueOfDescription(exerciseRecordDTO.target()));
		exercise.setEquipment(ExerciseEquipmentType.valueOfDescription(exerciseRecordDTO.equipment()));
		BeanUtils.copyProperties(exerciseRecordDTO, exercise);
		return ResponseEntity.status(HttpStatus.CREATED).body(exerciseRepository.save(exercise));
	}
	
	@GetMapping
	public ResponseEntity<List<Exercise>> getAllExercises(){
		List<Exercise> exerciseList = exerciseRepository.findAll();
		if(!exerciseList.isEmpty()) {
			for(Exercise exercise : exerciseList) {
				UUID id = exercise.getId();
				exercise.add(linkTo(methodOn(ExerciseController.class).getExercise(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(exerciseList);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getExercise(@PathVariable(value="id") UUID id){
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exercise not found");
		}
		exercise.get().add(linkTo(methodOn(ExerciseController.class).getAllExercises()).withRel("Exercises List"));
		return ResponseEntity.status(HttpStatus.OK).body(exercise);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateExercise(@PathVariable(value="id") UUID id, 
												 @RequestBody @Valid ExerciseRecordDTO exerciseRecordDTO) {
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exercise not found");
		}
		var getExerciseData = exercise.get();
		getExerciseData.setTarget(ExerciseTargetType.valueOfDescription(exerciseRecordDTO.target()));
		getExerciseData.setEquipment(ExerciseEquipmentType.valueOfDescription(exerciseRecordDTO.equipment()));
		BeanUtils.copyProperties(exerciseRecordDTO, getExerciseData);
		return ResponseEntity.status(HttpStatus.OK).body(exerciseRepository.save(getExerciseData));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteExercise(@PathVariable(value="id") UUID id){
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exercise not found");
		}
		exerciseRepository.delete(exercise.get());
		return ResponseEntity.status(HttpStatus.OK).body("Exercise deleted");
	}

}
