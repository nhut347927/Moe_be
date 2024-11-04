package com.moe.music.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class demoController {


//
//	    @Autowired
//	    private PostService postService;
//
//	    // Chỉ cho phép người dùng có quyền CREATE_POST thực hiện
//	    @PreAuthorize("hasAuthority('CREATE_POST')")
//	    @PostMapping
//	    public ResponseEntity<Post> createPost(@RequestBody Post post) {
//	        Post createdPost = postService.createPost(post);
//	        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
//	    }
//
//	    // Chỉ cho phép người dùng có quyền READ_POST thực hiện
//	    @PreAuthorize("hasAuthority('READ_POST')")
//	    @GetMapping
//	    public ResponseEntity<List<Post>> getAllPosts() {
//	        List<Post> posts = postService.getAllPosts();
//	        return ResponseEntity.ok(posts);
//	    }
//
//	    // Chỉ cho phép người dùng có quyền DELETE_POST thực hiện
//	    @PreAuthorize("hasAuthority('DELETE_POST')")
//	    @DeleteMapping("/{id}")
//	    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
//	        postService.deletePost(id);
//	        return ResponseEntity.noContent().build();
//	    }
//	

}
