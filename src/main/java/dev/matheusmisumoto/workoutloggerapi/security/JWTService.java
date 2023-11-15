package dev.matheusmisumoto.workoutloggerapi.security;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import dev.matheusmisumoto.workoutloggerapi.model.User;

@Service
public class JWTService {
	
	@Value("${api.secret.token.secret}")
	private String secret;
	
	public String generateToken(User user) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			String token = JWT.create()
					.withIssuer("workout-logger")
					.withSubject(user.getId().toString())
					.withExpiresAt(
							LocalDateTime.now().plusWeeks(1).toInstant(ZoneOffset.UTC)
					)
					.sign(algorithm);

			return token;
		} catch (JWTCreationException exception) {
			throw new RuntimeException("Error while generating token", exception);
		}
	}
	
	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.require(algorithm)
					.withIssuer("workout-logger")
					.build()
					.verify(token)
					.getSubject();
		} catch (JWTCreationException exception) {
			return "";
		}
	}

}
