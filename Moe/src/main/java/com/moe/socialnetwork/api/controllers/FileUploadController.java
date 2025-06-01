package com.moe.socialnetwork.api.controllers;

import com.moe.socialnetwork.api.dtos.FileUploadRequestDTO;
import com.moe.socialnetwork.api.services.ICloudinaryService;
import com.moe.socialnetwork.api.services.impl.CloudinaryServiceImpl;
import com.moe.socialnetwork.common.response.ResponseAPI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    private final ICloudinaryService cloudinaryService;

    public FileUploadController(CloudinaryServiceImpl cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    // API upload file tổng hợp
    @PostMapping("/upload")
    public ResponseEntity<ResponseAPI<String>> uploadFile(
            @ModelAttribute FileUploadRequestDTO request) throws IOException {
        String type = request.getType();
        MultipartFile file = request.getFile();
        String publicId;

        if ("image".equalsIgnoreCase(type)) {
            publicId = cloudinaryService.uploadImage(file);
        } else if ("video".equalsIgnoreCase(type)) {
            publicId = cloudinaryService.uploadVideo(file);
        } else if ("audio".equalsIgnoreCase(type)) {
            publicId = cloudinaryService.uploadAudio(file);
        } else {
            publicId = cloudinaryService.uploadAnyFile(file);
        }

        ResponseAPI<String> response = new ResponseAPI<>();
        response.setCode(200);
        response.setMessage("Upload successful");
        response.setData(publicId);
        return ResponseEntity.ok(response);
    }

    // Xóa file theo publicId
    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<ResponseAPI<String>> deleteFile(@PathVariable String publicId) throws IOException {
        boolean deleted = cloudinaryService.deleteFile(publicId);
        ResponseAPI<String> response = new ResponseAPI<>();
        if (deleted) {
            response.setCode(200);
            response.setMessage("Delete successful");
            response.setData(publicId);
            return ResponseEntity.ok(response);
        } else {
            response.setCode(200);
            response.setMessage("Delete failed");
            response.setData(publicId);
            return ResponseEntity.ok(response);
        }
    }
}
