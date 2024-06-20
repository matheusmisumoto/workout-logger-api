package dev.matheusmisumoto.workoutloggerapi.doc;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Bean
    OpenAPI myOpenAPI() {
    	Server prodServer = new Server();
        prodServer.setUrl("https://api.fitlogr.matheusmisumoto.dev");
        prodServer.setDescription("Server URL in Production environment");
    	
	    Contact contact = new Contact();
	    contact.setName("Matheus Misumoto");
	    contact.setUrl("https://matheusmisumoto.dev");

	    License gpl3License = new License().name("GPL-3.0 license").url("https://choosealicense.com/licenses/gpl-3.0/");

	    Info info = new Info()
	        .title("FitLogr API")
	        .version("1.4.0")
	        .contact(contact)
	        .description("API from FitLogr, an application project to log workout data to keep tracking of your fitness journey.")
	        .license(gpl3License);

	    return new OpenAPI().info(info).servers(List.of(prodServer));
	  }
	}