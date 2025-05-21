package com.moe.socialnetwork.api.services;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.common.models.User;


public interface IPostService {
	void createNewPost(MultipartFile videoFile,List<MultipartFile> imageFile,String content,boolean useOtherAudio,Long postId, User user) throws IOException;

	List<PostSearchResponseDTO> searchPosts(String keyword);

	void deletePost(Long postId, User user);
}
