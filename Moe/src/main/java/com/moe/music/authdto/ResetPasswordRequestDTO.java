package com.moe.music.authdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {
	
	private String token;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long.")
	private String newPassword;
	
	@NotBlank(message = "Confirm password is required.")
	@Size(min = 8, max = 255, message = "Confirm password must be between 8 and 255 characters long.")
	private String confirmNewPassword;
}
