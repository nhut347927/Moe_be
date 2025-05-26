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

import com.moe.socialnetwork.api.dtos.PostResponseDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.api.services.IPostService;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.response.ResponseAPI;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final IPostService postService;

    public PostController(IPostService postService) {
        this.postService = postService;
    };

    @GetMapping("/get-post")
    public ResponseEntity<ResponseAPI<List<PostResponseDTO>>> getPost(
            @AuthenticationPrincipal User user) {

        ResponseAPI<List<PostResponseDTO>> response = new ResponseAPI<>();

        List<PostResponseDTO> posts = postService.getPostList(user);
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Successful!");
        response.setData(posts);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping("/create-new-post")
    public ResponseEntity<ResponseAPI<String>> createNewPost(
            @AuthenticationPrincipal User user,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile, // videoFile -> videoPublicId
            @RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFile, // imageFile -> imgList
            @RequestPart(value = "title", required = false) String title, // .
            @RequestPart(value = "description", required = false) String description, // .
            @RequestPart(value = "useOtherAudio", required = false) String useOtherAudio, // . chưa biết
            @RequestPart(value = "postId", required = false) String postId) { // chưa biết
        ResponseAPI<String> response = new ResponseAPI<>();

        if (user == null) {
            response.setCode(HttpStatus.UNAUTHORIZED.value());
            response.setMessage("User is not authenticated!");
            response.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Boolean parsedUseOtherAudio = (useOtherAudio != null) ? Boolean.parseBoolean(useOtherAudio) : Boolean.FALSE;
        Long parsedPostId = (postId != null && !postId.isEmpty()) ? Long.parseLong(postId) : null;

        try {
            postService.createNewPost(videoFile, imageFile, title, description, parsedUseOtherAudio, parsedPostId, user);
            response.setCode(HttpStatus.OK.value());
            response.setMessage("Create post successful!");
            response.setData("Create post successful!");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (java.io.IOException e) {
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to create post: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseAPI<List<PostSearchResponseDTO>>> searchPosts(
            @RequestParam(required = false) String keyword) {

        ResponseAPI<List<PostSearchResponseDTO>> response = new ResponseAPI<>();
        List<PostSearchResponseDTO> posts = postService.searchPosts(keyword);
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Search successful!");
        response.setData(posts);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
