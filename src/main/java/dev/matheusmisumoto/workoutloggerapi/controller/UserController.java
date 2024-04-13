package dev.matheusmisumoto.workoutloggerapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.matheusmisumoto.workoutloggerapi.dto.RegisterDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.RegisterEditDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.UserShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/users")
public class UserController implements UserDetailsService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	JWTService jwtService;

	@PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody @Valid RegisterDTO data){
        if(userRepository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User();
        newUser.setLogin(data.login());
        newUser.setName(data.name());
        newUser.setPassword(encryptedPassword);
        newUser.setRole(data.role());
        newUser.setJoinedAt(LocalDateTime.now(Clock.systemUTC()));
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
	
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> userList = userRepository.findAll();
		if(!userList.isEmpty()) { 
			 for(User user : userList) {
				 user.add(linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel());
			 }
		}
		return ResponseEntity.status(HttpStatus.OK).body(userList);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getUser(@PathVariable(value="id") UUID id){
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		var userData = user.get();
		var totalLiftedRounded = 0;
		var totalLifted = userRepository.calculateUserTotalWeightLifted(userData.getId());
		if(totalLifted != null) {
			totalLiftedRounded = (int) Math.round(totalLifted);
		}
		var totalWorkouts = userRepository.totalWorkouts(userData.getId());
		
		List<Link> links = new ArrayList<Link>();
		links.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
		links.add(linkTo(methodOn(WorkoutController.class).latestUserWorkouts(id)).withRel("latestWorkouts"));
		links.add(linkTo(methodOn(WorkoutController.class).userWorkoutHistory(id, null)).withRel("workoutHistory"));
		
		var response = new UserShowDTO(userData.getId(),
									   userData.getName(),
									   userData.getLogin(),
									   userData.getOauthId(),
									   userData.getAvatarUrl(),
									   userData.getJoinedAt(),
									   totalWorkouts,
									   totalLiftedRounded,
									   links);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> editUser(HttpServletRequest request,
										   @PathVariable(value="id") UUID id,
										   @RequestBody RegisterEditDTO user){
		
		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));

		// Unauthorized if it's not the administrator or if the user is not editing his own account
		if(!loggedUserId.equals(id) && !request.isUserInRole("ROLE_ADMIN")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		
		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		var getUserData = userData.get();
		getUserData.setName(user.name());
		if(user.password() != null && !user.password().isEmpty()) {
			getUserData.setPassword(new BCryptPasswordEncoder().encode(user.password()));
		} 
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(getUserData));
	
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> removeUser(HttpServletRequest request,
											 @PathVariable(value="id") UUID id){

		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));
		
		// Unauthorized if it's not the administrator or if the user is not deleting his own account
		if(!loggedUserId.equals(id) && !request.isUserInRole("ROLE_ADMIN")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		userRepository.delete(userData.get());
		return ResponseEntity.status(HttpStatus.OK).body("User deleted");	
	}

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username);
    }
}
