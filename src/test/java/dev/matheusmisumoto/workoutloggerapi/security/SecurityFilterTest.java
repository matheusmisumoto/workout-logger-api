package dev.matheusmisumoto.workoutloggerapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.exceptions.TokenExpiredException;

import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;

@ExtendWith(MockitoExtension.class)
public class SecurityFilterTest {

	@Mock
	private JWTService jwtService;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SecurityFilter securityFilter;

	@BeforeEach
	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Expired or invalid JWT leaves context unauthenticated and does NOT call repository")
	void invalidToken_leavesSecurityContextEmpty_andDoesNotCallDatabase() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer expired.or.invalid.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(jwtService.validateToken("expired.or.invalid.token"))
				.thenThrow(new TokenExpiredException("The Token has expired", null));

		securityFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Token with non-UUID subject leaves context unauthenticated and does NOT call repository")
	void nonUuidTokenSubject_leavesSecurityContextEmpty_andDoesNotCallDatabase() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token.with.bad.subject");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(jwtService.validateToken("token.with.bad.subject")).thenReturn("not-a-valid-uuid");

		securityFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Database failure propagates exception directly and is NOT swallowed by filter")
	void databaseFailure_propagatesExceptionDirectly() {
		UUID userId = UUID.randomUUID();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(jwtService.validateToken("valid.token")).thenReturn(userId.toString());
		when(userRepository.findById(userId))
				.thenThrow(new DataAccessResourceFailureException("MariaDB connection refused"));

		assertThatThrownBy(() -> securityFilter.doFilter(request, response, filterChain))
				.isInstanceOf(DataAccessResourceFailureException.class)
				.hasMessageContaining("MariaDB connection refused");
	}

	@Test
	@DisplayName("Valid token successfully authenticates user into SecurityContext")
	void validToken_authenticatesUserSuccessfully() throws Exception {
		UUID userId = UUID.randomUUID();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		User user = new User();
		user.setId(userId);
		user.setName("Test User");
		user.setLogin("test_user");
		user.setRole(UserRoleType.MEMBER);

		when(jwtService.validateToken("valid.token")).thenReturn(userId.toString());
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		securityFilter.doFilter(request, response, filterChain);

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.isAuthenticated()).isTrue();
		assertThat(authentication.getPrincipal()).isEqualTo(user);
	}

	@Test
	@DisplayName("Missing Authorization header proceeds unauthenticated without errors")
	void missingAuthorizationHeader_proceedsWithoutAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		securityFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any());
	}
}

