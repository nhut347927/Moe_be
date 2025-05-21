package com.moe.socialnetwork.api.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRCMRequestDTO {
    private String userId;
    private String postId;
    private String createdAt;

    private String userAvatar;
    private String userDisplayName;

    private String postType; // "VIDEO" hoặc "IMAGE"
    private String videoUrl;
    private List<String> imageUrls;
    private String caption;

    private String likeCount;
    private String commentCount;
    private String playlistCount;

    private String audioUrl;
    private String audioOwnerAvatar;
    private String audioOwnerName;
    private String audioId;

    private List<CommentDTO> comments;
}