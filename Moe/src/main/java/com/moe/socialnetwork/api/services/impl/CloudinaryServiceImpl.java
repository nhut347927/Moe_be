package com.moe.socialnetwork.api.services.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.moe.socialnetwork.api.services.ICloudinaryService;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

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

    private String getFileUrl(String publicId, String resourceType) {
        return cloudinary.url().secure(true).resourceType(resourceType).generate(publicId);
    }

    public File convertMultipartToFile(MultipartFile file, String filename) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + filename);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    public File downloadFileFromCloudinary(String publicId, String filename, String fileType) throws IOException {
        // Xác định loại file cần tải: video hoặc audio
        String fileUrl = getFileUrl(publicId, fileType);

        // Tạo file tạm
        File file = new File(System.getProperty("java.io.tmpdir"), filename);

        // Xóa file nếu tồn tại để tránh lỗi giữ file cũ
        if (file.exists()) {
            file.delete();
        }

        // Tải file từ Cloudinary
        try (InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Lỗi khi tải " + fileType + " từ Cloudinary: " + e.getMessage(), e);
        }

        return file;
    }

    public File extractAudioFromVideo(File videoFile) throws IOException {
        // Tạo file âm thanh tạm, tránh ghi đè
        File audioFile = File.createTempFile("extracted_audio_", ".mp3");

        if (audioFile.exists()) {
            audioFile.delete();
        }

        try {
            FFmpeg ffmpeg = new FFmpeg("D:/ffmpeg/bin/ffmpeg.exe");
            FFprobe ffprobe = new FFprobe("D:/ffmpeg/bin/ffprobe.exe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoFile.getAbsolutePath()) // Input video
                    .addOutput(audioFile.getAbsolutePath()) // Output audio
                    .setFormat("mp3") // Định dạng MP3
                    .setAudioCodec("libmp3lame") // Codec MP3 chất lượng cao
                    .addExtraArgs("-q:a", "2") // Chất lượng tốt nhất (-q:a 2)
                    .addExtraArgs("-vn") // Loại bỏ video, chỉ lấy âm thanh
                    .done();

            // Thực thi FFmpeg
            new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();
            return audioFile;
        } catch (Exception e) {
            throw new IOException("Lỗi khi trích xuất âm thanh: " + e.getMessage(), e);
        }
    }

    public File mergeVideoWithAudio(File video, File audio) throws IOException {
        File outputVideo = File.createTempFile("merged_video_", ".mp4");

        if (outputVideo.exists()) {
            outputVideo.delete();
        }

        try {
            FFmpeg ffmpeg = new FFmpeg("D:/ffmpeg/bin/ffmpeg.exe");
            FFprobe ffprobe = new FFprobe("D:/ffmpeg/bin/ffprobe.exe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(video.getAbsolutePath()) // Input video
                    .addInput(audio.getAbsolutePath()) // Input audio
                    .overrideOutputFiles(true) // Ghi đè file nếu tồn tại
                    .addOutput(outputVideo.getAbsolutePath()) // File output
                    .setFormat("mp4") // Định dạng file đầu ra
                    .setVideoCodec("copy") // Giữ nguyên codec video
                    .setAudioCodec("aac") // Chuyển audio sang AAC
                    .addExtraArgs("-map", "0:v:0", "-map", "1:a:0") // Đồng bộ audio và video
                    .addExtraArgs("-c:v", "copy", "-c:a", "aac", "-strict", "experimental") // Giữ nguyên video, mã hóa
                                                                                            // audio AAC
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();
            return outputVideo;
        } catch (Exception e) {
            throw new IOException("Lỗi khi ghép video và audio: " + e.getMessage(), e);
        }
    }
}