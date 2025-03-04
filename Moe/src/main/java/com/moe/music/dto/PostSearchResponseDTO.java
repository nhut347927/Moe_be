package com.moe.music.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSearchResponseDTO {
    private String image;
    private String displayName;
    private String content;
    private String postId;
    private String video;
}
