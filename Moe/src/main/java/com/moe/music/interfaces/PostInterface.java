package com.moe.music.interfaces;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import com.moe.music.dto.PostSearchResponseDTO;
import com.moe.music.model.User;

public interface PostInterface {
	void createNewPost(MultipartFile videoFile,List<MultipartFile> imageFile,String content,boolean useOtherAudio,Long postId, User user) throws IOException;

	List<PostSearchResponseDTO> searchPosts(String keyword);

	void deletePost(Long postId, User user);
}
