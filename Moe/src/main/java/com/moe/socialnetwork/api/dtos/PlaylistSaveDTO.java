package com.moe.socialnetwork.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSaveDTO {
    private String code;
    @NotBlank(message = "Name playlist cannot be blank")
    private String name;
    @NotBlank(message = "Name playlist cannot be blank")
    private String description;
    private Boolean isPublic = true;
    @NotBlank(message = "Image playlist cannot be blank")
    private String image;
}
