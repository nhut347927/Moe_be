package com.moe.socialnetwork.api.queue.post;

import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreationMessage {
    private Long userId;
    private PostCreateRepuestDTO dto;
}
