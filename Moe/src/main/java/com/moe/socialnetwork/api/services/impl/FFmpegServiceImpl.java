package com.moe.socialnetwork.api.services.impl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO.FFmpegMergeParams;
import com.moe.socialnetwork.api.services.ICloudinaryService;
import com.moe.socialnetwork.api.services.IFFmpegService;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

@Service
public class FFmpegServiceImpl implements IFFmpegService {

    private final ICloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;
    private final String ffmpegPath = "ffmpeg";

    public FFmpegServiceImpl(ICloudinaryService cloudinaryService, Cloudinary cloudinary) {
        this.cloudinaryService = cloudinaryService;
        this.cloudinary = cloudinary;
    }

    @Override
    public String mergeAndUpload(FFmpegMergeParams params) throws IOException, InterruptedException {
        // 1. Tải về file local
        File downloadedVideo = downloadFileFromCloudinary(params.getVideoPublicId(), "video.mp4", "video");
        File downloadedAudio = downloadFileFromCloudinary(params.getAudioPublicId(), "audio.mp3", "video");

        // 2. Tạo file tạm
        File trimmedVideo = File.createTempFile("video_trimmed", ".mp4");
        File trimmedAudio = File.createTempFile("audio_trimmed", ".aac");
        File adjustedAudio = File.createTempFile("audio_adjusted", ".aac");
        File mergedOutput = File.createTempFile("merged_output", ".mp4");

        try {
            // === Trim video chính xác & giữ audio gốc ===
            runProcess(new ProcessBuilder(
                    ffmpegPath, "-y",
                    "-ss", String.valueOf(params.getVideoCutStart()),
                    "-to", String.valueOf(params.getVideoCutEnd()),
                    "-i", downloadedVideo.getAbsolutePath(),
                    "-c:v", "libx264", "-preset", "veryfast", "-crf", "23",
                    "-c:a", "aac", "-b:a", "128k", // giữ audio gốc trong video
                    trimmedVideo.getAbsolutePath()));

            // === Trim audio ===
            runProcess(new ProcessBuilder(
                    ffmpegPath, "-y", "-i", downloadedAudio.getAbsolutePath(),
                    "-ss", String.valueOf(params.getAudioCutStart()),
                    "-to", String.valueOf(params.getAudioCutEnd()),
                    "-c:a", "aac", "-b:a", "128k",
                    trimmedAudio.getAbsolutePath()));

            // === Điều chỉnh âm lượng & delay cho audio phụ ===
            String audioFilter;
            double audioVolume = params.getAudioVolume() != null ? params.getAudioVolume() : 1.0;
            double offsetMs = params.getAudioOffset() != null ? params.getAudioOffset() * 1000 : 0;

            if (offsetMs > 0) {
                audioFilter = String.format(Locale.US, "adelay=%.3f|%.3f,volume=%.3f", offsetMs, offsetMs, audioVolume);
            } else {
                audioFilter = String.format(Locale.US, "volume=%.3f", audioVolume);
            }

            runProcess(new ProcessBuilder(
                    ffmpegPath, "-y", "-i", trimmedAudio.getAbsolutePath(),
                    "-af", audioFilter,
                    "-ac", "2",
                    "-c:a", "aac",
                    adjustedAudio.getAbsolutePath()));

            // === Trộn audio phụ và audio của video gốc ===
            double videoVolume = params.getVideoVolume() != null ? params.getVideoVolume() : 1.0;

            String filterComplex = String.format(Locale.US,
                    "[0:a]volume=%.3f[a0];[1:a]volume=1.0[a1];[a0][a1]amix=inputs=2:duration=first:dropout_transition=3[aout]",
                    videoVolume);

            List<String> mergeCmd = new ArrayList<>(List.of(
                    ffmpegPath, "-y",
                    "-i", trimmedVideo.getAbsolutePath(), // 0: video + audio gốc
                    "-i", adjustedAudio.getAbsolutePath(), // 1: audio phụ
                    "-filter_complex", filterComplex,
                    "-map", "0:v:0",
                    "-map", "[aout]",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    mergedOutput.getAbsolutePath()));

            runProcess(new ProcessBuilder(mergeCmd));

            // === Upload kết quả lên Cloudinary ===
            return cloudinaryService.uploadVideo(mergedOutput);

        } finally {
            // === Xoá các file tạm ===
            Stream.of(
                    downloadedVideo, downloadedAudio,
                    trimmedVideo, trimmedAudio,
                    adjustedAudio, mergedOutput).forEach(f -> {
                        if (f != null && f.exists())
                            f.delete();
                    });
        }
    }

    private void runProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg command failed with exit code " + exitCode);
        }
    }

    private String getFileUrl(String publicId, String resourceType) {
        return cloudinary.url().secure(true).resourceType(resourceType).generate(publicId);
    }

    public File downloadFileFromCloudinary(String publicId, String filename, String fileType) {
        String fileUrl = getFileUrl(publicId, fileType);
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (file.exists())
            file.delete();

        try (InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from Cloudinary", e);
        }

        return file;
    }

    public File extractAudioFromVideo(File videoFile) throws IOException {
        File audioFile = File.createTempFile("extracted_audio_", ".mp3");
        if (audioFile.exists())
            audioFile.delete();

        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffmpegPath);

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoFile.getAbsolutePath())
                    .addOutput(audioFile.getAbsolutePath())
                    .setFormat("mp3")
                    .setAudioCodec("libmp3lame")
                    .addExtraArgs("-q:a", "2")
                    .addExtraArgs("-vn")
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();
            return audioFile;
        } catch (Exception e) {
            throw new IOException("Lỗi khi trích xuất âm thanh: " + e.getMessage(), e);
        }
    }

    public File mergeVideoWithAudio(File video, File audio) throws IOException {
        File outputVideo = File.createTempFile("merged_video_", ".mp4");
        if (outputVideo.exists())
            outputVideo.delete();

        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffmpegPath);

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(video.getAbsolutePath())
                    .addInput(audio.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(outputVideo.getAbsolutePath())
                    .setFormat("mp4")
                    .setVideoCodec("copy")
                    .setAudioCodec("aac")
                    .addExtraArgs("-map", "0:v:0", "-map", "1:a:0")
                    .addExtraArgs("-c:v", "copy", "-c:a", "aac", "-strict", "experimental")
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();
            return outputVideo;
        } catch (Exception e) {
            throw new IOException("Lỗi khi ghép video và audio: " + e.getMessage(), e);
        }
    }

}
