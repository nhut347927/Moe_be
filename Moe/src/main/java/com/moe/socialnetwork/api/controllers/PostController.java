package com.moe.socialnetwork.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moe.socialnetwork.api.dtos.PostRCMRequestDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.api.services.IPostService;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.response.ResponseAPI;
import com.moe.socialnetwork.exception.AppException;


@RestController
@RequestMapping("/api/post")
public class PostController {

	private IPostService postService;

	public PostController(IPostService postService) {
		this.postService = postService;
	};

	@GetMapping("/get-post")
	public ResponseEntity<ResponseAPI<List<PostRCMRequestDTO>>> getPost(
		@AuthenticationPrincipal User user) {

		ResponseAPI<List<PostRCMRequestDTO>> response = new ResponseAPI<>();
		try {
			List<PostRCMRequestDTO> posts = postService.getPostDetail(user);
			response.setCode(HttpStatus.OK.value());
			response.setMessage("Successful!");
			response.setData(posts);
			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);

		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}



	@PostMapping("/create-new-post")
	public ResponseEntity<ResponseAPI<String>> createNewPost(
			@AuthenticationPrincipal User user,
			@RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
			@RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFile,
			@RequestPart(value = "content", required = false) String content,
			@RequestPart(value = "useOtherAudio", required = false) String useOtherAudio,
			@RequestPart(value = "postId", required = false) String postId
	) {
		ResponseAPI<String> response = new ResponseAPI<>();

		try {
			if (user == null) {
				response.setCode(HttpStatus.UNAUTHORIZED.value());
				response.setMessage("User is not authenticated!");
				response.setData(null);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			Boolean parsedUseOtherAudio = (useOtherAudio != null) ? Boolean.parseBoolean(useOtherAudio) : null;
			Long parsedPostId = (postId != null && !postId.isEmpty()) ? Long.parseLong(postId) : null;
	
			postService.createNewPost(videoFile,imageFile, content, parsedUseOtherAudio, parsedPostId , user);

			response.setCode(HttpStatus.OK.value());
			response.setMessage("Create post successful!");
			response.setData("Create post successful!");

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/search")
	public ResponseEntity<ResponseAPI<List<PostSearchResponseDTO>>> searchPosts(
			@RequestParam(required = false) String keyword) {

		ResponseAPI<List<PostSearchResponseDTO>> response = new ResponseAPI<>();
		try {
			List<PostSearchResponseDTO> posts = postService.searchPosts(keyword);
			response.setCode(HttpStatus.OK.value());
			response.setMessage("Search successful!");
			response.setData(posts);
			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);

		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
