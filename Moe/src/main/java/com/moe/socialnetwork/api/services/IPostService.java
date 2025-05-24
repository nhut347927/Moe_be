package com.moe.socialnetwork.api.services;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.moe.socialnetwork.api.dtos.PostResponseDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.common.models.User;


public interface IPostService {
	void createNewPost(MultipartFile videoFile, List<MultipartFile> imageFile, String title, String description,
			boolean useOtherAudio, Long postId, User user) throws IOException;

	List<PostSearchResponseDTO> searchPosts(String keyword);
	
	List<PostResponseDTO> getPostList(User user);

	void deletePost(Long postId, User user);
}
