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
import com.moe.socialnetwork.api.queue.post.PostCreationMessage;
import com.moe.socialnetwork.api.services.IPostService;
import com.moe.socialnetwork.api.queue.post.PostCreationProducer;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.response.ResponseAPI;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final IPostService postService;
    private final PostCreationProducer postCreationProducer;

    public PostController(IPostService postService, PostCreationProducer postCreationProducer) {
        this.postService = postService;
        this.postCreationProducer = postCreationProducer;
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
            @RequestBody @Valid PostCreateRepuestDTO dto) {

        PostCreationMessage job = new PostCreationMessage(user.getId(), dto);
        postCreationProducer.enqueue(job); // Đẩy vào hàng đợi

        ResponseAPI<String> response = new ResponseAPI<>();
        response.setCode(HttpStatus.ACCEPTED.value());
        response.setMessage("Post has been queued for creation.");
        response.setData("Please wait...");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
