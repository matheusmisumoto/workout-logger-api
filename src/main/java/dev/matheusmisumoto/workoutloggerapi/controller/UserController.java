package dev.matheusmisumoto.workoutloggerapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.web.server.ResponseStatusException;

import dev.matheusmisumoto.workoutloggerapi.dto.RegisterDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.RegisterEditDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.UserListDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.UserShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController implements UserDetailsService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	JWTService jwtService;

	@PostMapping("/register")
	@Operation(summary = "Register a new user")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "201", 
        		description = "User created"
        ),
        @ApiResponse(
        		responseCode = "400", 
        		description = "User already exists"
        )
	})
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data){
        if(userRepository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User();
        newUser.setLogin(data.login());
        newUser.setName(data.name());
        newUser.setPassword(encryptedPassword);
        // newUser.setRole(data.role());
        newUser.setJoinedAt(LocalDateTime.now(Clock.systemUTC()));
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
	
	@GetMapping
	@Operation(summary = "Retrieve a list of all users")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Show users data"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden", 
        		content = @Content
        )
	})
	public ResponseEntity<List<UserListDTO>> getAllUsers(){
		List<User> users = userRepository.findAll();
		List<UserListDTO> userList = new ArrayList<UserListDTO>();

		userList = users.stream().map(
				user -> {
					List<Link> links = new ArrayList<Link>();
					links.add(linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel());
					
					UserListDTO setDTO = new UserListDTO(user.getId(),
		 					  user.getName(),
		 					  user.getUsername(),
		 					  user.getOauthId(),
		 					  user.getOauthProvider(),
		 					  user.getAvatarUrl(),
		 					  user.getJoinedAt(),
		 					  user.getAuthorities(),
		 					  links
					);
					return setDTO;
				}).collect(Collectors.toList());			
		return ResponseEntity.status(HttpStatus.OK).body(userList);
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Retrieve user profile information")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "Show user data", 
        		content = @Content(schema = @Schema(implementation = UserShowDTO.class))
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden", 
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "User not found", 
        		content = @Content
        )
	})
	public ResponseEntity<UserShowDTO> getUser(@PathVariable(value="id") UUID id){
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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
		links.add(linkTo(methodOn(WorkoutController.class).userWorkoutHistory(id, "")).withRel("workoutHistory"));
		
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
	@Operation(summary = "Edit user profile information")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "User updated", 
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden", 
        		content = @Content
        ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "User not found", 
        		content = @Content
        )
	})
	public ResponseEntity<Void> editUser(HttpServletRequest request,
										   @PathVariable(value="id") UUID id,
										   @RequestBody RegisterEditDTO user){
		
		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));

		// Forbidden if it's not the administrator or if the user is not editing his own account
		if(!loggedUserId.equals(id) && !request.isUserInRole("ROLE_ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		
		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		var getUserData = userData.get();
		getUserData.setName(user.name());
		
		// Update user role if the request is made by an administrator
		if(request.isUserInRole("ROLE_ADMIN")) {
			getUserData.setRole(user.role());
		}
		
		if(user.password() != null && !user.password().isEmpty()) {
			getUserData.setPassword(new BCryptPasswordEncoder().encode(user.password()));
		}
		userRepository.save(getUserData);
		
		return ResponseEntity.status(HttpStatus.OK).build();	
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Remove user")
	@ApiResponses({
        @ApiResponse(
        		responseCode = "200", 
        		description = "User removed"
        ),
        @ApiResponse(
        		responseCode = "403", 
        		description = "Forbidden"
       ),
        @ApiResponse(
        		responseCode = "404", 
        		description = "User not found"
       )
	})
	public ResponseEntity<Void> removeUser(HttpServletRequest request,
											 @PathVariable(value="id") UUID id){

		// Retrieve logged user ID from JWT
		var token = request.getHeader("Authorization").replace("Bearer ", "");
		var loggedUserId = UUID.fromString(jwtService.validateToken(token));
		
		// Unauthorized if it's not the administrator or if the user is not deleting his own account
		if(!loggedUserId.equals(id) && !request.isUserInRole("ROLE_ADMIN")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		userRepository.delete(userData.get());
		return ResponseEntity.status(HttpStatus.OK).build();	
	}

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username);
    }
}
