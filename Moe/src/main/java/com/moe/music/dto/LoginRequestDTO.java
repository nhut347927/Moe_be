package com.moe.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must be a valid email address.")
	@NotBlank(message = "Email is required.")
	@Size(max = 100, message = "Email must not exceed 100 characters.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long.")
	private String password;
}
