package com.moe.socialnetwork.api.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private String userCode;
    private String postCode;
    private String createdAt;

    private String userAvatar;
    private String userDisplayName;
    private String userName;

    private String postType; // "VIDEO" hoặc "IMAGE"
    private String videoUrl;
    private List<String> imageUrls;
    private String title;
    private String description;

    private String likeCount;
    private String commentCount;
    private Boolean isAddPlaylist;

    private String audioUrl;
    private String audioOwnerAvatar;
    private String audioOwnerDisplayName;
    private String audioCode;

    private List<CommentDTO> comments;
}