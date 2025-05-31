package com.moe.socialnetwork.api.controllers;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moe.socialnetwork.api.dtos.UploadFileDTO;
import com.moe.socialnetwork.api.services.impl.CloudinaryServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/upload")
public class demoController {
	private final CloudinaryServiceImpl cloudinaryService;
	

	public demoController(CloudinaryServiceImpl cloudinaryService) {
		this.cloudinaryService = cloudinaryService;
		
	}

	// 📌 Upload ảnh
	@PostMapping("/image")
	public ResponseEntity<String> uploadImage(@ModelAttribute @Valid UploadFileDTO request) throws IOException {
		String publicId = cloudinaryService.uploadImage(request.getFile());
		return ResponseEntity.ok(publicId);
	}

	// 📌 Upload video
	@PostMapping("/video")
	public ResponseEntity<String> uploadVideo(@ModelAttribute @Valid UploadFileDTO request) throws IOException {
		String publicId = cloudinaryService.uploadVideo(request.getFile());
		return ResponseEntity.ok(publicId);
	}

	// 📌 Xóa file theo public_id
	@DeleteMapping("/delete/{publicId}")
	public ResponseEntity<String> deleteFile(@PathVariable String publicId) throws IOException {
		boolean deleted = cloudinaryService.deleteFile(publicId);
		return deleted ? ResponseEntity.ok("File deleted: " + publicId)
				: ResponseEntity.badRequest().body("Failed to delete file: " + publicId);
	}

}
