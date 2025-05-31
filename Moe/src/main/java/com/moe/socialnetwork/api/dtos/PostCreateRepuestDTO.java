package com.moe.socialnetwork.api.dtos;

import jakarta.validation.Valid;
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
    @Size(max = 150, message = "Title must not exceed 150 characters")
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
    @Pattern(regexp = "^(PRIVATE|PUBLIC)$", message = "Visibility must be 'ONLY_YOU', 'FRIEND', or 'PUBLIC'")
    private String visibility;

    private String audioCode;
    @Valid
    private FFmpegMergeParams ffmpegMergeParams;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FFmpegMergeParams {

        private String videoPublicId;

        @DecimalMin(value = "0.0", message = "videoCutStart must be >= 0")
        private Double videoCutStart = 0.0;

        @NotNull(message = "videoCutEnd is required")
        @DecimalMin(value = "0.1", message = "videoCutEnd must be >= 0.1")
        private Double videoCutEnd;

        private String audioPublicId;

        @DecimalMin(value = "0.0", message = "audioCutStart must be >= 0")
        private Double audioCutStart = 0.0;

        @DecimalMin(value = "0.1", message = "audioCutEnd must be >= 0.1")
        private Double audioCutEnd;

        @DecimalMin(value = "0.0", message = "videoVolume must be >= 0")
        @DecimalMax(value = "2.0", message = "videoVolume must be <= 2")
        private Double videoVolume = 1.0;

        @DecimalMin(value = "0.0", message = "audioVolume must be >= 0")
        @DecimalMax(value = "2.0", message = "audioVolume must be <= 2")
        private Double audioVolume = 1.0;

        @DecimalMin(value = "0.0", message = "audioOffset must be >= 0")
        private Double audioOffset = 0.0;
    }

}
