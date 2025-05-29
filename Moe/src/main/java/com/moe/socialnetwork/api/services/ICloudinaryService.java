package com.moe.socialnetwork.api.services;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {
    String uploadImage(MultipartFile file) throws IOException;

    String uploadVideo(MultipartFile file) throws IOException;

    String uploadAudio(MultipartFile file) throws IOException;

     String uploadImage(File file) throws IOException;

    String uploadVideo(File file) throws IOException;

    String uploadAudio(File file) throws IOException;
   
    String uploadAnyFile(MultipartFile file) throws IOException;

    boolean deleteFile(String publicId) throws IOException;
}