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

import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;

@RestController
@RequestMapping("/v1/user")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@PostMapping
	public ResponseEntity<User> addUser(@RequestBody User user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
	}
	
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers(){
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.findAll());
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
