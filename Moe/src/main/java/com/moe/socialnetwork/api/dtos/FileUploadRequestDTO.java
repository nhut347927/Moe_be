package com.moe.socialnetwork.api.dtos;

import org.springframework.web.multipart.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequestDTO {
    private MultipartFile file;
    private String type; // "image", "video", "audio", "any"
}
