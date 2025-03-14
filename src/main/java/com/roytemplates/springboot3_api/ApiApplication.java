// Package declaration for the application
package com.roytemplates.springboot3_api;

// Import required Spring Boot classes
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

// Main annotation that enables auto-configuration and component scanning
@SpringBootApplication
@Slf4j
public class ApiApplication {

	// Main method - entry point of the application
	public static void main(String[] args) {
		// Launch the Spring Boot application
		log.info("Starting Springboot2 API application...");
		SpringApplication.run(ApiApplication.class, args);
	}

}
