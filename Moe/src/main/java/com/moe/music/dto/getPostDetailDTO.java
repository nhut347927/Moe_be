package com.moe.music.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class getPostDetailDTO {
	private String avatar;
	private List<String> img;
	private String userName;
	private String postCount;
	private String followers;
	private String bio;
	private String favoriteSong;
	private String songImage;
	private String likes;
	private String commentsCount;
	private List<CommentDTO> comments;
	private String audio;
	private String ownerAudioPostId;
	private String typePost;
	private String video;
}
