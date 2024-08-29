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
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import dev.matheusmisumoto.workoutloggerapi.dto.ExerciseRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.type.ExerciseEquipmentType;
import dev.matheusmisumoto.workoutloggerapi.type.ExerciseTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/exercises")
@Tag(name = "Exercises", description = "Exercise library management endpoints")
public class ExerciseController {
	
	@Autowired
	ExerciseRepository exerciseRepository;
	
	@PostMapping
	@Operation(summary = "Register a new exercise")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "201", 
        		description = "Exercise created"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<Exercise> saveExercise(@RequestBody @Valid ExerciseRecordDTO exerciseRecordDTO) {
		var exercise = new Exercise();
		exercise.setTarget(ExerciseTargetType.valueOfDescription(exerciseRecordDTO.target()));
		exercise.setEquipment(ExerciseEquipmentType.valueOfDescription(exerciseRecordDTO.equipment()));
		BeanUtils.copyProperties(exerciseRecordDTO, exercise);
		return ResponseEntity.status(HttpStatus.CREATED).body(exerciseRepository.save(exercise));
	}
	
	@GetMapping
	@Operation(summary = "Retrieve a list of all exercises")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Show all exercises"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<List<Exercise>> getAllExercises(){
		List<Exercise> exerciseList = exerciseRepository.findAllByOrderByNameAsc();
		if(!exerciseList.isEmpty()) {
			for(Exercise exercise : exerciseList) {
				UUID id = exercise.getId();
				exercise.add(linkTo(methodOn(ExerciseController.class).getExercise(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(exerciseList);
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Retrieve exercise information")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Show exercise information"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "Not Found",
        		content = @Content
        )
	})	
	public ResponseEntity<Optional<Exercise>> getExercise(@PathVariable(value="id") UUID id){
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
		}
		exercise.get().add(linkTo(methodOn(ExerciseController.class).getExercise(id)).withSelfRel());
		exercise.get().add(linkTo(methodOn(ExerciseController.class).getExercisesByMuscle(exercise.get()
				.getTarget()
				.replace(" ", "-")
				.toLowerCase()
		)).withRel("listByMuscle"));
		exercise.get().add(linkTo(methodOn(ExerciseController.class).getExercisesByEquipment(exercise.get()
				.getEquipment()
				.replace(" ", "-")
				.toLowerCase()
				)).withRel("listByEquipment"));
		exercise.get().add(linkTo(methodOn(ExerciseController.class).getAllExercises()).withRel("exerciseList"));
		return ResponseEntity.status(HttpStatus.OK).body(exercise);
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update exercise information")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Show updated exercise information"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "Not Found",
        		content = @Content
        )
	})
	public ResponseEntity<Exercise> updateExercise(@PathVariable(value="id") UUID id, 
												 @RequestBody @Valid ExerciseRecordDTO exerciseRecordDTO) {
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
		}
		var getExerciseData = exercise.get();
		getExerciseData.setTarget(ExerciseTargetType.valueOfDescription(exerciseRecordDTO.target()));
		getExerciseData.setEquipment(ExerciseEquipmentType.valueOfDescription(exerciseRecordDTO.equipment()));
		BeanUtils.copyProperties(exerciseRecordDTO, getExerciseData);
		return ResponseEntity.status(HttpStatus.OK).body(exerciseRepository.save(getExerciseData));
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Remove exercise")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Exercise removed",
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "400", 
        		description = "Bad Request",
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "Not Found",
        		content = @Content
        )
	})
	public ResponseEntity<String> deleteExercise(@PathVariable(value="id") UUID id){
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if(exercise.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
		}
		try {
			exerciseRepository.delete(exercise.get());
			return ResponseEntity.status(HttpStatus.OK).body("Exercise deleted");	
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exercise cannot be deleted while in use");
		}
	}
	
	@GetMapping("/muscle")
	@Operation(summary = "List all muscles")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "List of all muscles",
        		content = @Content(schema = @Schema(implementation = ObjectNode.class))
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<ObjectNode> getMuscles(){
		ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        
        for (Enum<?> constant : ExerciseTargetType.values()) {
            json.put(constant.name(), ((ExerciseTargetType) constant).getDescription());
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(json);
	}
	
	@GetMapping("/muscle/{muscle}")
	@Operation(summary = "List exercises that target a given muscle")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Exercises targeting a given muscle"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<List<Exercise>> getExercisesByMuscle(@PathVariable(value="muscle") String target){
		target = target.replace("-", " ");
		List<Exercise> exerciseList = exerciseRepository.findByTarget(ExerciseTargetType.valueOfDescription(target));
		if(!exerciseList.isEmpty()) {
			for(Exercise exercise : exerciseList) {
				UUID id = exercise.getId();
				exercise.add(linkTo(methodOn(ExerciseController.class).getExercise(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(exerciseList);
	}
	
	@GetMapping("/equipment")
	@Operation(summary = "List all equipments")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "List of all types of equipments",
        		content = @Content(schema = @Schema(implementation = ObjectNode.class))
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<ObjectNode> getEquipments(){
		ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        
        for (Enum<?> constant : ExerciseEquipmentType.values()) {
            json.put(constant.name(), ((ExerciseEquipmentType) constant).getDescription());
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(json);
	}

	@GetMapping("/equipment/{equipment}")
	@Operation(summary = "List all exercises that uses a given equipment")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Exercises using a given equipment"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden",
        		content = @Content
        )
	})
	public ResponseEntity<List<Exercise>> getExercisesByEquipment(@PathVariable(value="equipment") String equipment){
		equipment = equipment.replace("-", " ");
		List<Exercise> exerciseList = exerciseRepository.findByEquipment(ExerciseEquipmentType.valueOfDescription(equipment));
		if(!exerciseList.isEmpty()) {
			for(Exercise exercise : exerciseList) {
				UUID id = exercise.getId();
				exercise.add(linkTo(methodOn(ExerciseController.class).getExercise(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(exerciseList);
	}
}
