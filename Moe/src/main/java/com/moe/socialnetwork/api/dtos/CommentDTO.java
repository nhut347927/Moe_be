package com.moe.socialnetwork.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private String commentCode;
    private String userAvatar;
    private String content;
    private String displayName;
    private String createdAt;
    private List<ReplyDTO> replies;
}
