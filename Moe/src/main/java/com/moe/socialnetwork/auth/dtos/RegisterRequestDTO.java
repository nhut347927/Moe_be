package com.moe.socialnetwork.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must be a valid email address!")
	@NotBlank(message = "Email is required!")
	@Size(max = 100, message = "Email must not exceed 100 characters!")
	private String email;

	@NotBlank(message = "Password is required!")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long!")
	private String password;

	@NotBlank(message = "Confirm password is required!")
	@Size(min = 8, max = 255, message = "Confirm password must be between 8 and 255 characters long!")
	private String confirmPassword;

	@NotBlank(message = "Display name is required!")
	@Size(min = 3, max = 100, message = "Display name must be between 3 and 100 characters.")
	@Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9]*$", message = "Display name cannot start or end with special characters or spaces.")
	@Pattern(regexp = "^(?!admin|root|superuser).*", message = "Display name cannot be a reserved keyword.")
	private String displayName;

}
