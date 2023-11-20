package dev.matheusmisumoto.workoutloggerapi.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import dev.matheusmisumoto.workoutloggerapi.dto.OAuthCodeDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.TokenDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.UserShowDTO;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import dev.matheusmisumoto.workoutloggerapi.util.OAuthUtil;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/user")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	JWTService jwtService;
	
	@Autowired
	OAuthUtil oAuthUtil;

	@PostMapping("/oauth")
	public ResponseEntity<TokenDTO> oAuthLogin(@RequestBody @Valid OAuthCodeDTO codeDTO) {
		
		var oAuthUser = oAuthUtil.getOAuthData(codeDTO);

		var oauthUserId = oAuthUser.id();
		Optional<User> checkUser = userRepository.findByOauthId(oauthUserId);
		
		if(checkUser.isEmpty()) {
			var newUser = new User();
			newUser.setOauthId(oAuthUser.id());
			newUser.setName(oAuthUser.name());
			newUser.setAvatarUrl(oAuthUser.avatar_url());
			userRepository.save(newUser);
			checkUser = userRepository.findByOauthId(oauthUserId);
		}
		
		
		var token = jwtService.generateToken(checkUser.get());
		var response = new TokenDTO(token);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers(){
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getUser(@PathVariable(value="id") UUID id){
		Optional<User> user = userRepository.findById(id);
		if(user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		var userData = user.get();
		var totalLifted = userRepository.calculateUserTotalWeightLifted(userData.getId());
		var totalWorkouts = userRepository.totalWorkouts(userData.getId());
		var response = new UserShowDTO(userData.getId(),
									   userData.getName(),
									   userData.getLogin(),
									   userData.getOauthId(),
									   userData.getAvatarUrl(),
									   totalWorkouts,
									   totalLifted);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> editUser(@PathVariable(value="id") UUID id,
										   @RequestBody User user){
		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> removeUser(@PathVariable(value="id") UUID id){
		Optional<User> userData = userRepository.findById(id);
		if(userData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		userRepository.delete(userData.get());
		return ResponseEntity.status(HttpStatus.OK).body("User deleted");	
	}

}
