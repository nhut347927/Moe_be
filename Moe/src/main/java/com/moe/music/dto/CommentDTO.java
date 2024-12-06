package com.moe.music.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

	private String avatar;
	private String initials;
	private String userName;
	private String content;
	private String time;

}
