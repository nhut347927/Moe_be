package com.moe.music.utility;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageStorageService.class);
    private final String URL_DOMAIN = "http://localhost:8080";
    private final Path rootLocation = Paths.get("static/images");

    public String getImageUrl(String fileName) {
        return String.format("%s/images/%s", URL_DOMAIN, fileName);
    }

    public String saveImage(MultipartFile file) {
        String fileName = generateUniqueFileName(".jpg");

        try {
            Files.createDirectories(rootLocation);
            Files.copy(file.getInputStream(), rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            logger.error("Error saving image: " + e.getMessage());
            return null;
        }
    }

    public byte[] downloadImage(String fileName) throws IOException {
        return Files.readAllBytes(rootLocation.resolve(fileName));
    }

    public void deleteImage(String fileName) {
        try {
            Files.deleteIfExists(rootLocation.resolve(fileName));
        } catch (IOException e) {
            logger.error("Error deleting image: " + e.getMessage());
        }
    }

    public boolean containsEmptyFile(List<MultipartFile> files) {
        return files.stream().anyMatch(MultipartFile::isEmpty);
    }

    public byte[] decodeBase64Image(String base64Image) {
        return Base64.decodeBase64(base64Image);
    }

    public String saveImageFromBase64(String base64Image) {
        String fileName = generateUniqueFileName(".jpg");
        byte[] imageBytes = decodeBase64Image(base64Image);

        try {
            Files.createDirectories(rootLocation);
            Files.write(rootLocation.resolve(fileName), imageBytes);
            return fileName;
        } catch (IOException e) {
            logger.error("Error saving base64 image: " + e.getMessage());
            return null;
        }
    }

    private String generateUniqueFileName(String extension) {
        return new Date().getTime() + extension;
    }
}
