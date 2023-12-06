package dev.matheusmisumoto.workoutloggerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class})
public class WorkoutLoggerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkoutLoggerApiApplication.class, args);
	}
	
	@GetMapping("/")
	public ResponseEntity<String> indexResponse() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

}
