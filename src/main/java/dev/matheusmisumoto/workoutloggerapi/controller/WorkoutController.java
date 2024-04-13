package dev.matheusmisumoto.workoutloggerapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.matheusmisumoto.workoutloggerapi.dto.PreviousStatsDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.PreviousStatsWorkoutsDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutRecordDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.WorkoutSetShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutSetRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import dev.matheusmisumoto.workoutloggerapi.type.WorkoutStatusType;
import dev.matheusmisumoto.workoutloggerapi.util.WorkoutUtil;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/v1/workouts")
public class WorkoutController {
	
	@Autowired
	WorkoutRepository workoutRepository;
	
	@Autowired
	ExerciseRepository exerciseRepository;
	
	@Autowired
	WorkoutSetRepository workoutSetRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	JWTService jwtService;
	
	@PostMapping
	public ResponseEntity<Object> saveWorkout(HttpServletRequest request,
											  @RequestBody WorkoutRecordDTO workoutRecordDTO) {
		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));
		
		// Save metadata
		var workout = new Workout();
		workout.setStatus(WorkoutStatusType.valueOfDescription(workoutRecordDTO.status()));
		workout.setDate(OffsetDateTime.now());
		workout.setUser(userRepository.findById(loggedUserId).get());
		BeanUtils.copyProperties(workoutRecordDTO, workout);
		var workoutMetadata = workoutRepository.save(workout);
		
		var sets = new WorkoutUtil();
		sets.recordSets(exerciseRepository, workoutSetRepository, workoutMetadata, workoutRecordDTO);
				
		return ResponseEntity.status(HttpStatus.CREATED).body(workoutMetadata);
	}
	
	@GetMapping("/user/{id}/all")
	public ResponseEntity<Object> allUserWorkouts(@PathVariable(value="id") UUID id) {
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		var allWorkouts = workoutRepository.findAllByUserOrderByDateDesc(user.get());
		var responseBuilder = new WorkoutUtil();

		List<Object> response = allWorkouts.stream()
				.map(workout -> {
					return responseBuilder.buildWorkoutCardJSON(workout, workoutSetRepository);
				}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/user/{id}/history")
	public ResponseEntity<Object> userWorkoutHistory(@PathVariable(value="id") UUID id,
													 @RequestParam(required = false) String page) {
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		// Set defaults
		int resultsPerPage = 10;
		int pageNumber = 1;
		
		// Retrieve total number of pages to return on JSON 
		// to avoid call other routes and do the math on front-end
		int totalPages = (int) Math.ceil(Double.valueOf(workoutRepository.countByUser(user.get())) / Double.valueOf(resultsPerPage));
		
		// Check the page parameter
		if(page != null && !page.isEmpty()) {
			if(Integer.parseInt(page) > 0) {
				pageNumber = Integer.parseInt(page);
			}
		}
		
		if(pageNumber > totalPages) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}		

		PageRequest pagination = PageRequest.of(pageNumber - 1, resultsPerPage, Sort.by("date").descending());
		var allWorkouts = workoutRepository.findAllByUser(user.get(), pagination);
		
		var responseBuilder = new WorkoutUtil();

		List<Object> response = allWorkouts.stream()
				.map(workout -> {
					return responseBuilder.buildWorkoutCardJSON(workout, workoutSetRepository);
				}).collect(Collectors.toList());

		
		List<Link> links = new ArrayList<Link>();
		links.add(linkTo(methodOn(UserController.class).getUser(user.get().getId())).withRel("userProfile"));
		
		
		// Wraps the list of workouts on a new JSON containing the page information
		ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonBuilder = objectMapper.createObjectNode();
		jsonBuilder.put("currentPage", pageNumber);
		jsonBuilder.put("totalPages", totalPages);
		jsonBuilder.putPOJO("workouts", response);
		jsonBuilder.putPOJO("links", links);


		return ResponseEntity.status(HttpStatus.OK).body(jsonBuilder);
	}

	@GetMapping("/user/{id}/last")
	public ResponseEntity<Object> latestUserWorkouts(@PathVariable(value="id") UUID id) {
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		var allWorkouts = workoutRepository.findTop10ByUserOrderByDateDesc(user.get());
		var responseBuilder = new WorkoutUtil();

		List<Object> response = allWorkouts.stream()
				.map(workout -> {
					return responseBuilder.buildWorkoutCardJSON(workout, workoutSetRepository);
				}).collect(Collectors.toList());
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}	
	
	@GetMapping("/user/{userid}/{id}")
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
	
	@GetMapping("/user/{userid}/exercise/{exerciseid}")
	public ResponseEntity<Object> getLastStatsFromExercise(@PathVariable(value="userid") UUID userid,
														   @PathVariable(value="exerciseid") UUID exerciseid){
		Optional<User> user = userRepository.findById(userid);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		Optional<Exercise> exercise = exerciseRepository.findById(exerciseid);
		if(exercise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exercise not found");
		}
		
		var getWorkouts = workoutRepository.findLatestWorkoutsWithExercise(userid, exerciseid);
		
		List<PreviousStatsWorkoutsDTO> workouts = getWorkouts.stream()
										.map( workout -> {
											var getSets = workoutSetRepository.findByWorkoutAndExerciseOrderBySetOrderAsc(workout, exercise.get());
											
											List<WorkoutSetShowDTO> sets = getSets.stream().map(set -> {
												WorkoutSetShowDTO setDTO = new WorkoutSetShowDTO(
														set.getType(),
														set.getWeight(),
														set.getReps()
														);
												return setDTO;
											}).collect(Collectors.toList());
											
											PreviousStatsWorkoutsDTO workoutDTO = new PreviousStatsWorkoutsDTO(
													workout.getDate(),
													sets												
											);
										return workoutDTO;
										}).collect(Collectors.toList());
		PreviousStatsDTO response = new PreviousStatsDTO(exerciseid,
														 exercise.get().getName(),
														 workouts);
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateWorkout(HttpServletRequest request,
												@PathVariable(value="id") UUID id,
											    @RequestBody WorkoutRecordDTO workoutRecordDTO){
		
		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));
		
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}

		// Prepare metadata to be updated
		var workoutData = workout.get();

		// Unauthorized if it's not the administrator or if the user is not deleting his own account
		if(!loggedUserId.equals(workoutData.getUser().getId()) && !request.isUserInRole("ROLE_ADMIN")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

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
	public ResponseEntity<Object> deleteWorkout(HttpServletRequest request,
												@PathVariable(value="id") UUID id) {
		
		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));
		
		Optional<Workout> workout = workoutRepository.findById(id);
		if(workout.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workout not found");
		}

		// Unauthorized if it's not the administrator or if the user is not deleting his own account
		if(!loggedUserId.equals(workout.get().getUser().getId()) && !request.isUserInRole("ROLE_ADMIN")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
				
		workoutRepository.delete(workout.get());
		return ResponseEntity.status(HttpStatus.OK).body("Workout deleted");
	}
}
