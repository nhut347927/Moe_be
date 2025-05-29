package com.moe.socialnetwork.api.services;

import java.util.List;

import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO;
import com.moe.socialnetwork.api.dtos.PostResponseDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.common.models.User;

public interface IPostService {
	Boolean createNewPost(PostCreateRepuestDTO dto, User user);

	List<PostSearchResponseDTO> searchPosts(String keyword);

	List<PostResponseDTO> getPostList(User user);

	void deletePost(Long postId, User user);
}
