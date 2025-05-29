package com.moe.socialnetwork.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO;
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
            @RequestBody PostCreateRepuestDTO postCreateRequestDTO) {
        ResponseAPI<String> response = new ResponseAPI<>();

        postService.createNewPost(postCreateRequestDTO, user);
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Create post successful!");
        response.setData("Create post successful!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
