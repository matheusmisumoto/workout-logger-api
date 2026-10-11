package dev.matheusmisumoto.workoutloggerapi.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {
	
	@Autowired
	JWTService jwtService;
	
	@Autowired
	UserRepository userRepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
		
		var token = this.recoverToken(request);
		if(token != null) {
			UUID userId = null;
			try {
				var userid = jwtService.validateToken(token);
				if(userid != null && !userid.isBlank()) {
					userId = UUID.fromString(userid);
				}
			} catch (JWTVerificationException | IllegalArgumentException e) {
				// Invalid token or malformed UUID: request remains unauthenticated
				userId = null;
			}

			// Repository call is intentionally outside the try-catch:
			// Database or connection errors will properly propagate rather than being masked as auth failures.
			if(userId != null) {
				var user = userRepository.findById(userId);
				if(user.isPresent()) {
					var authentication = new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}
		filterChain.doFilter(request, response);
	}
	
	private String recoverToken(HttpServletRequest request) {
		var authHeader = request.getHeader("Authorization");
		if(authHeader == null || !authHeader.startsWith("Bearer ")) return null;
		return authHeader.substring(7);
	}

}
