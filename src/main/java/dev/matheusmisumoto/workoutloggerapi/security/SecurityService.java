package dev.matheusmisumoto.workoutloggerapi.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class SecurityService {

	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserRepository userRepository;

	public UUID getLoggedUserId() {
		return getLoggedUserId(getCurrentHttpRequest());
	}

	public UUID getLoggedUserId(HttpServletRequest request) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof User user) {
				return user.getId();
			} else if (principal instanceof String principalStr) {
				try {
					return UUID.fromString(principalStr);
				} catch (IllegalArgumentException e) {
					var userDetails = userRepository.findByLogin(principalStr);
					if (userDetails instanceof User u) {
						return u.getId();
					}
				}
			}
		}

		if (request == null) {
			request = getCurrentHttpRequest();
		}

		if (request != null) {
			var authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				var token = authHeader.substring(7);
				try {
					var subject = jwtService.validateToken(token);
					if (subject != null) {
						return UUID.fromString(subject);
					}
				} catch (Exception ignored) {
				}
			}
		}
		return null;
	}

	public boolean isAdmin() {
		return isAdmin(getCurrentHttpRequest());
	}

	public boolean isAdmin(HttpServletRequest request) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			boolean hasAdminRole = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			if (hasAdminRole) {
				return true;
			}
		}
		if (request == null) {
			request = getCurrentHttpRequest();
		}
		return request != null && (request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ADMIN"));
	}

	public boolean isUserOwnerOrAdmin(UUID targetUserId) {
		return isUserOwnerOrAdmin(targetUserId, getCurrentHttpRequest());
	}

	public boolean isUserOwnerOrAdmin(UUID targetUserId, HttpServletRequest request) {
		if (targetUserId == null) {
			return false;
		}
		if (isAdmin(request)) {
			return true;
		}
		UUID loggedUserId = getLoggedUserId(request);
		return loggedUserId != null && loggedUserId.equals(targetUserId);
	}

	public void validateOwnershipOrAdmin(UUID targetUserId, String errorMessage) {
		validateOwnershipOrAdmin(targetUserId, getCurrentHttpRequest(), errorMessage);
	}

	public void validateOwnershipOrAdmin(UUID targetUserId, HttpServletRequest request, String errorMessage) {
		if (!isUserOwnerOrAdmin(targetUserId, request)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage != null ? errorMessage : "Access denied");
		}
	}

	private HttpServletRequest getCurrentHttpRequest() {
		var requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
			return servletRequestAttributes.getRequest();
		}
		return null;
	}
}
