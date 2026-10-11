package dev.matheusmisumoto.workoutloggerapi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.matheusmisumoto.workoutloggerapi.model.Exercise;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.model.Workout;
import dev.matheusmisumoto.workoutloggerapi.repository.ExerciseRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutRepository;
import dev.matheusmisumoto.workoutloggerapi.repository.WorkoutSetRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import dev.matheusmisumoto.workoutloggerapi.type.ExerciseEquipmentType;
import dev.matheusmisumoto.workoutloggerapi.type.ExerciseTargetType;
import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;
import dev.matheusmisumoto.workoutloggerapi.type.WorkoutStatusType;

@SpringBootTest
public class WorkoutSecurityIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WorkoutRepository workoutRepository;

	@Autowired
	private WorkoutSetRepository workoutSetRepository;

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private JWTService jwtService;

	private User userA;
	private User userB;
	private User adminUser;

	private String tokenUserA;
	private String tokenUserB;
	private String tokenAdmin;

	private Workout workoutB;
	private Exercise exercise;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		workoutSetRepository.deleteAll();
		workoutRepository.deleteAll();
		userRepository.deleteAll();
		exerciseRepository.deleteAll();

		// Create User A
		userA = new User();
		userA.setName("User Alpha");
		userA.setLogin("user_alpha");
		userA.setPassword("secret");
		userA.setRole(UserRoleType.MEMBER);
		userA.setJoinedAt(LocalDateTime.now());
		userA = userRepository.save(userA);

		// Create User B
		userB = new User();
		userB.setName("User Beta");
		userB.setLogin("user_beta");
		userB.setPassword("secret");
		userB.setRole(UserRoleType.MEMBER);
		userB.setJoinedAt(LocalDateTime.now());
		userB = userRepository.save(userB);

		// Create Admin User
		adminUser = new User();
		adminUser.setName("Admin User");
		adminUser.setLogin("admin_user");
		adminUser.setPassword("secret");
		adminUser.setRole(UserRoleType.ADMIN);
		adminUser.setJoinedAt(LocalDateTime.now());
		adminUser = userRepository.save(adminUser);

		// Generate JWTs
		tokenUserA = "Bearer " + jwtService.generateToken(userA);
		tokenUserB = "Bearer " + jwtService.generateToken(userB);
		tokenAdmin = "Bearer " + jwtService.generateToken(adminUser);

		// Create Exercise
		exercise = new Exercise();
		exercise.setName("Bench Press");
		exercise.setTarget(ExerciseTargetType.CHEST);
		exercise.setEquipment(ExerciseEquipmentType.BARBELL);
		exercise = exerciseRepository.save(exercise);

		// Create Workout for User B
		workoutB = new Workout();
		workoutB.setName("Chest Day");
		workoutB.setDate(OffsetDateTime.now());
		workoutB.setDuration(45);
		workoutB.setStatus(WorkoutStatusType.COMPLETED);
		workoutB.setUser(userB);
		workoutB = workoutRepository.save(workoutB);
	}

	@Test
	@DisplayName("User A cannot read User B's all workouts list")
	void userACannotReadAllWorkoutsFromUserB() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/all")
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot read User B's workout history")
	void userACannotReadHistoryFromUserB() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/history")
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot read User B's latest workouts")
	void userACannotReadLatestFromUserB() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/last")
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot read User B's specific workout log")
	void userACannotReadSpecificWorkoutFromUserB() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/" + workoutB.getId())
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot read User B's exercise statistics")
	void userACannotReadExerciseStatsFromUserB() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/exercise/" + exercise.getId())
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot read User B's profile")
	void userACannotReadProfileFromUserB() throws Exception {
		mockMvc.perform(get("/v1/users/" + userB.getId())
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User B can read their own all workouts list")
	void userBCanReadOwnAllWorkouts() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/all")
				.header("Authorization", tokenUserB))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("User B can read their own specific workout")
	void userBCanReadOwnSpecificWorkout() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/" + workoutB.getId())
				.header("Authorization", tokenUserB))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("User B can read their own profile")
	void userBCanReadOwnProfile() throws Exception {
		mockMvc.perform(get("/v1/users/" + userB.getId())
				.header("Authorization", tokenUserB))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Admin can read User B's all workouts list")
	void adminCanReadUserBWorkouts() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/all")
				.header("Authorization", tokenAdmin))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Admin can read User B's profile")
	void adminCanReadUserBProfile() throws Exception {
		mockMvc.perform(get("/v1/users/" + userB.getId())
				.header("Authorization", tokenAdmin))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("User A cannot edit User B's workout")
	void userACannotEditWorkoutFromUserB() throws Exception {
		String body = "{\"name\":\"Hacked Workout\",\"duration\":30,\"status\":\"COMPLETED\",\"exercises\":[]}";
		mockMvc.perform(put("/v1/workouts/" + workoutB.getId())
				.header("Authorization", tokenUserA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot delete User B's workout")
	void userACannotDeleteWorkoutFromUserB() throws Exception {
		mockMvc.perform(delete("/v1/workouts/" + workoutB.getId())
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot edit User B's user profile")
	void userACannotEditUserBProfile() throws Exception {
		String body = "{\"name\":\"Hacked Name\",\"role\":\"MEMBER\"}";
		mockMvc.perform(put("/v1/users/" + userB.getId())
				.header("Authorization", tokenUserA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("User A cannot delete User B's profile")
	void userACannotDeleteUserBProfile() throws Exception {
		mockMvc.perform(delete("/v1/users/" + userB.getId())
				.header("Authorization", tokenUserA))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Invalid or malformed JWT token is gracefully rejected as forbidden")
	void invalidTokenIsGracefullyRejected() throws Exception {
		mockMvc.perform(get("/v1/workouts/user/" + userB.getId() + "/all")
				.header("Authorization", "Bearer invalid.malformed.token"))
				.andExpect(status().isForbidden());
	}
}
