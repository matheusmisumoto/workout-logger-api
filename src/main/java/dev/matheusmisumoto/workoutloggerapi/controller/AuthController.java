package dev.matheusmisumoto.workoutloggerapi.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.matheusmisumoto.workoutloggerapi.dto.LoginDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.OAuthCodeDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.TokenDTO;
import dev.matheusmisumoto.workoutloggerapi.model.User;
import dev.matheusmisumoto.workoutloggerapi.repository.UserRepository;
import dev.matheusmisumoto.workoutloggerapi.security.JWTService;
import dev.matheusmisumoto.workoutloggerapi.type.OAuthProviderType;
import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;
import dev.matheusmisumoto.workoutloggerapi.util.OAuthUtil;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
    private AuthenticationManager authenticationManager;

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
			newUser.setOauthProvider(OAuthProviderType.GITHUB);
			newUser.setRole(UserRoleType.MEMBER);
			newUser.setJoinedAt(LocalDateTime.now(Clock.systemUTC()));
			userRepository.save(newUser);
			checkUser = userRepository.findByOauthId(oauthUserId);
		}
		
		var auth = new UsernamePasswordAuthenticationToken(checkUser.get().getId().toString(), null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		var token = jwtService.generateToken(checkUser.get());
		var response = new TokenDTO(token);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	
	@PostMapping("/login")
	public ResponseEntity<Object> usernamePasswordLogin(@RequestBody @Valid LoginDTO data){
		var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
       
    	var auth = authenticationManager.authenticate(usernamePassword);			
        var token = jwtService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(new TokenDTO(token));
	}

}
