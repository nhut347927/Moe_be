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
public class VideoStorageService {

    private static final Logger logger = LoggerFactory.getLogger(VideoStorageService.class);
    private final String URL_DOMAIN = "http://localhost:8080";
    private final Path rootLocation = Paths.get("static/videos");

    public String getVideoUrl(String fileName) {
        return String.format("%s/videos/%s", URL_DOMAIN, fileName);
    }

    public String saveVideo(MultipartFile file) {
        String fileName = generateUniqueFileName(".mp4");

        try {
            Files.createDirectories(rootLocation);
            Files.copy(file.getInputStream(), rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            logger.error("Error saving video: " + e.getMessage());
            return null;
        }
    }

    public byte[] downloadVideo(String fileName) throws IOException {
        return Files.readAllBytes(rootLocation.resolve(fileName));
    }

    public void deleteVideo(String fileName) {
        try {
            Files.deleteIfExists(rootLocation.resolve(fileName));
        } catch (IOException e) {
            logger.error("Error deleting video: " + e.getMessage());
        }
    }

    public boolean containsEmptyFile(List<MultipartFile> files) {
        return files.stream().anyMatch(MultipartFile::isEmpty);
    }

    public byte[] decodeBase64Video(String base64Video) {
        return Base64.decodeBase64(base64Video);
    }

    public String saveVideoFromBase64(String base64Video) {
        String fileName = generateUniqueFileName(".mp4");
        byte[] videoBytes = decodeBase64Video(base64Video);

        try {
            Files.createDirectories(rootLocation);
            Files.write(rootLocation.resolve(fileName), videoBytes);
            return fileName;
        } catch (IOException e) {
            logger.error("Error saving base64 video: " + e.getMessage());
            return null;
        }
    }

    private String generateUniqueFileName(String extension) {
        return new Date().getTime() + extension;
    }
}
