package com.moe.music.dtoauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

	@NotBlank(message = "Old password is required.")
	@Size(min = 8, max = 255, message = "Old password must be between 8 and 255 characters long.")
	private String oldPassword;

	@NotBlank(message = "New password is required.")
	@Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters long.")
	private String newPassword;
}
