package com.moe.socialnetwork.api.controllers;

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

    // Upload ảnh
    @PostMapping("/upload-image")
    public ResponseEntity<ResponseAPI<String>> uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {
        String publicId = cloudinaryService.uploadImage(file);
        ResponseAPI<String> response = new ResponseAPI<>();
        response.setCode(200);
        response.setMessage("Upload successful");
        response.setData(publicId);
        return ResponseEntity.ok(response);
    }

    // Upload video
    @PostMapping("/upload-video")
    public ResponseEntity<ResponseAPI<String>> uploadVideo(@RequestParam("file") MultipartFile file)
            throws IOException {
        String publicId = cloudinaryService.uploadVideo(file);
        ResponseAPI<String> response = new ResponseAPI<>();
        response.setCode(200);
        response.setMessage("Upload successful");
        response.setData(publicId);
        return ResponseEntity.ok(response);
    }

    // Upload audio/file bất kỳ
    @PostMapping("/upload-audio")
    public ResponseEntity<ResponseAPI<String>> uploadAudio(@RequestParam("file") MultipartFile file)
            throws IOException {
        String publicId = cloudinaryService.uploadAudio(file);
        ResponseAPI<String> response = new ResponseAPI<>();
        response.setCode(200);
        response.setMessage("Upload successful");
        response.setData(publicId);
        return ResponseEntity.ok(response);
    }

    // API upload file bất kỳ (Excel, Word, PDF, ...)
    @PostMapping("/upload-any")
    public ResponseEntity<ResponseAPI<String>> uploadAnyFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        String publicId = cloudinaryService.uploadAnyFile(file);
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
