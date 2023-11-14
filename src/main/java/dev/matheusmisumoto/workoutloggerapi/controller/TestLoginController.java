package dev.matheusmisumoto.workoutloggerapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestLoginController {

	@GetMapping("/")
	public ResponseEntity<String> index(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@AuthenticationPrincipal OAuth2User oauth2User) {
		var name = oauth2User.getName();
		var clientName = authorizedClient.getClientRegistration().getClientName();
		var att = oauth2User.getAttributes();
		return ResponseEntity.status(HttpStatus.OK).body(name + "," + clientName + "," + att);
	}
}
