package com.moe.socialnetwork.api.services.impl;

import java.io.File;
import java.io.FileOutputStream;
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

    /**
     * Upload an image file to Cloudinary.
     *
     * @param file The image file to upload.
     * @return The public ID of the uploaded image.
     * @throws IOException If an error occurs during the upload.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "format", "jpg",
                        "folder", "images"));
        return (String) response.get("public_id");
    }

    /**
     * Upload a video file to Cloudinary.
     *
     * @param file The video file to upload.
     * @return The public ID of the uploaded video.
     * @throws IOException If an error occurs during the upload.
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "format", "mp4",
                        "folder", "videos"));
        return (String) response.get("public_id");
    }

    /**
     * Upload an audio file to Cloudinary.
     *
     * @param file The audio file to upload.
     * @return The public ID of the uploaded audio.
     * @throws IOException If an error occurs during the upload.
     */
    public String uploadAudio(MultipartFile file) throws IOException {
        Map<?, ?> response = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "format", "mp3",
                        "folder", "audios"));
        return (String) response.get("public_id");
    }

    /**
     * Delete a file from Cloudinary.
     *
     * @param publicId The public ID of the file to delete.
     * @return True if the file was successfully deleted, false otherwise.
     * @throws IOException If an error occurs during the deletion.
     */
    public boolean deleteFile(String publicId) throws IOException {
        Map<?, ?> response = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(response.get("result"));
    }

    /**
     * Convert a MultipartFile to a File.
     *
     * @param file     The MultipartFile to convert.
     * @param filename The name of the resulting file.
     * @return The converted File.
     * @throws IOException If an error occurs during the conversion.
     */
    public File convertMultipartToFile(MultipartFile file, String filename) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + filename);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}