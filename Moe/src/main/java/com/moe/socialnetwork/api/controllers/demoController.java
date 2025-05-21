package com.moe.socialnetwork.api.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moe.socialnetwork.api.dtos.UploadFileDTO;
import com.moe.socialnetwork.api.services.impl.CloudinaryServiceImpl;
import com.moe.socialnetwork.api.services.impl.VideoProcessingServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/upload")
public class demoController {
	private final CloudinaryServiceImpl cloudinaryService;
	private final VideoProcessingServiceImpl videoProcessingService;

	public demoController(CloudinaryServiceImpl cloudinaryService) {
		this.cloudinaryService = cloudinaryService;
		this.videoProcessingService = new VideoProcessingServiceImpl();
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

	/**
	 * Endpoint tách âm thanh từ video. URL: /extract-audio
	 */
	@PostMapping("/extract-audio")
	public ResponseEntity<Resource> extractAudio(@RequestParam("video") MultipartFile videoFile) {
		try {
			// Lưu file video tạm thời
			Path tempVideoFile = Files.createTempFile("video", ".mp4");
			Files.write(tempVideoFile, videoFile.getBytes());

			// Gọi service để tách âm thanh
			Path audioFile = videoProcessingService.extractAudio(tempVideoFile);

			// Trả file audio về client
			Resource resource = new FileSystemResource(audioFile.toFile());
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audio.mp3")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
		} catch (IOException | InterruptedException e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	/**
	 * Endpoint ghép âm thanh vào video. URL: /merge-audio
	 */
	@PostMapping("/merge-audio")
	public ResponseEntity<Resource> mergeAudio(@RequestParam("video") MultipartFile videoFile,
			@RequestParam("audio") MultipartFile audioFile) {
		try {
			// Lưu file video tạm thời
			Path tempVideoFile = Files.createTempFile("video", ".mp4");
			Files.write(tempVideoFile, videoFile.getBytes());

			// Lưu file audio tạm thời
			Path tempAudioFile = Files.createTempFile("audio", ".mp3");
			Files.write(tempAudioFile, audioFile.getBytes());

			// Gọi service để ghép âm thanh vào video
			Path mergedVideoFile = videoProcessingService.mergeAudio(tempVideoFile, tempAudioFile);

			// Trả file video đã ghép về client
			Resource resource = new FileSystemResource(mergedVideoFile.toFile());
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.mp4")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
		} catch (IOException | InterruptedException e) {
			return ResponseEntity.status(500).body(null);
		}
	}
}
