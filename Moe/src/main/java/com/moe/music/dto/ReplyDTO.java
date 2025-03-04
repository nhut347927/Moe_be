package com.moe.music.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
    private String commentId;
    private String userAvatar;
    private String content;
    private String displayName;
    private String createdAt;
}