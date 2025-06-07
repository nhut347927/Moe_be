package com.moe.socialnetwork.api.dtos;

import lombok.Data;
import java.util.List;

@Data
public class PlaylistDTO {
    private String code;
    private String name;
    private String description;
    private Boolean isPublic;
    private String image;
    private List<PlaylistPostDTO> posts;
} 
