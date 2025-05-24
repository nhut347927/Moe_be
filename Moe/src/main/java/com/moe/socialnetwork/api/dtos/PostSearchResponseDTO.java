package com.moe.socialnetwork.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSearchResponseDTO {
    private String image;
    private String displayName;
    private String title;
    private String postCode;
    private String videoUrl;
}
