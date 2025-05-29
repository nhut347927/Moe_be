package com.moe.socialnetwork.api.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.moe.socialnetwork.api.services.ICloudinaryService;

@Service
public class CloudinaryServiceImpl implements ICloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "format", "jpg",
                        "folder", "images"));
        return (String) response.get("public_id");
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "format", "mp4",
                        "folder", "videos"));
        return (String) response.get("public_id");
    }

    public String uploadAudio(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "format", "mp3",
                        "folder", "audios"));
        return (String) response.get("public_id");
    }

    public String uploadAnyFile(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "files"));
        return (String) response.get("public_id");
    }

    public boolean deleteFile(String publicId) throws IOException {
        Map<?, ?> response = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(response.get("result"));
    }

    public String uploadImage(File file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file,
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "format", "jpg", // Chuyển đổi tất cả thành JPG
                        "folder", "images"));
        return (String) response.get("public_id");
    }

    public String uploadVideo(File file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file,
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "format", "mp4",
                        "folder", "videos"));
        return (String) response.get("public_id");
    }

    public String uploadAudio(File file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file,
                ObjectUtils.asMap(
                        "resource_type", "video", // Cloudinary nhận audio là video
                        "format", "mp3",
                        "folder", "audios"));
        return (String) response.get("public_id");
    }

}