package dev.matheusmisumoto.workoutloggerapi.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import dev.matheusmisumoto.workoutloggerapi.dto.OAuthCodeDTO;
import dev.matheusmisumoto.workoutloggerapi.dto.OAuthUserDTO;

@Service
public class OAuthUtil {
	
	@Value("${github.client-id}")
	private String clientId;
		
	@Value("${github.client-secret}")
	private String clientSecret;
	
	public OAuthUserDTO getOAuthData(OAuthCodeDTO codeDTO){
		String tokenUrl = "https://github.com/login/oauth/access_token";
	    String requestUrl = tokenUrl + "?client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + codeDTO.code();
	    var requestToken = new RestTemplate().postForEntity(requestUrl, null, String.class);
	    var accessToken = "";
	     
	    String[] parts = requestToken.getBody().split("&");
	    for (String part : parts) {
	     	if (part.startsWith("access_token=")) {
	     		accessToken = part.substring("access_token=".length());
	     	}
	    }
	        
	    String userUrl = "https://api.github.com/user";
	    var headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + accessToken);

	    try {
	    	HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		    var response = new RestTemplate().exchange(userUrl, HttpMethod.GET, requestEntity, OAuthUserDTO.class);
		    return response.getBody();
		} catch (RestClientException e) {
			throw new RuntimeException("Unauthorized access to GitHub: ", e);
		}
	 
	}
}
