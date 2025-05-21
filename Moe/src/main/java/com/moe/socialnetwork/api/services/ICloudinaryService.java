package com.moe.socialnetwork.api.services;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {

    /**
     * Upload an image file to Cloudinary.
     *
     * @param file The image file to upload.
     * @return The public ID of the uploaded image.
     * @throws IOException If an error occurs during the upload.
     */
    String uploadImage(MultipartFile file) throws IOException;

    /**
     * Upload a video file to Cloudinary.
     *
     * @param file The video file to upload.
     * @return The public ID of the uploaded video.
     * @throws IOException If an error occurs during the upload.
     */
    String uploadVideo(MultipartFile file) throws IOException;

    /**
     * Upload an audio file to Cloudinary.
     *
     * @param file The audio file to upload.
     * @return The public ID of the uploaded audio.
     * @throws IOException If an error occurs during the upload.
     */
    String uploadAudio(MultipartFile file) throws IOException;

    /**
     * Delete a file from Cloudinary.
     *
     * @param publicId The public ID of the file to delete.
     * @return True if the file was successfully deleted, false otherwise.
     * @throws IOException If an error occurs during the deletion.
     */
    boolean deleteFile(String publicId) throws IOException;

    /**
     * Convert a MultipartFile to a File.
     *
     * @param file     The MultipartFile to convert.
     * @param filename The name of the resulting file.
     * @return The converted File.
     * @throws IOException If an error occurs during the conversion.
     */
    File convertMultipartToFile(MultipartFile file, String filename) throws IOException;
}