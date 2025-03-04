package com.moe.music.dto;

import org.springframework.web.multipart.MultipartFile;

import com.moe.music.customvalidator.FileSize;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileDTO {
	@NotNull(message = "File không được để trống!")
	@FileSize(max = 5 * 1024 * 1024, message = "File ảnh không được vượt quá 5MB!")
	private MultipartFile file;

}