package com.moe.socialnetwork.api.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRepuestDTO {

    @NotBlank(message = "Title must not be empty")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    @NotBlank(message = "Post type is required")
    @Pattern(regexp = "^(VID|IMG)$", message = "Post type must be either 'VID' or 'IMG'")
    private String postType;

    @NotNull(message = "isUseOtherAudio must not be null")
    private Boolean isUseOtherAudio;

    private String videoPublicId;

    @Min(value = 0, message = "Video thumbnail must be a positive number")
    private Integer videoThumbnail = 0;

    private List<String> imgList;

    private List<String> tagList;

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "^(ONLY_YOU|FRIEND|PUBLIC)$", message = "Visibility must be 'ONLY_YOU', 'FRIEND', or 'PUBLIC'")
    private String isPublic;

    private String audioCode;

    private FFmpegMergeParams ffmpegMergeParams;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FFmpegMergeParams {
        private String videoPublicId;
        private Integer videoCutStart;
        private Integer videoCutEnd;
        private String audioPublicId;
        private Integer audioCutStart;
        private Integer audioCutEnd;
        private Integer audioOffset;
        private Double videoVolume;
        private Double audioVolume;
    }
}
