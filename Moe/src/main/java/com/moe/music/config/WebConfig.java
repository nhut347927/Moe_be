package com.moe.music.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application, including CORS settings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	// Inject domain from application properties
	@Value("${cors.allowed.origin}")
	private String allowedOrigin;

	/**
	 * Adds CORS configuration for all application endpoints.
	 *
	 * @param registry CorsRegistry object to configure CORS
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOriginPatterns(allowedOrigin)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*")
				.exposedHeaders("Authorization").allowCredentials(true).maxAge(3600);
	}
}
