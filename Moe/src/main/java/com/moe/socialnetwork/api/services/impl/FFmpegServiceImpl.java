package com.moe.socialnetwork.api.services.impl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private final String ffmpegPath = "src\\main\\resources\\ffmpeg-n6.1-latest-win64-lgpl-shared-6.1\\bin\\ffmpeg.exe";

    public FFmpegServiceImpl(ICloudinaryService cloudinaryService, Cloudinary cloudinary) {
        this.cloudinaryService = cloudinaryService;
        this.cloudinary = cloudinary;
    }

    @Override
    public String mergeAndUpload(FFmpegMergeParams params) throws IOException, InterruptedException {
        String videoInput = getFileUrl(params.getVideoPublicId(), "video");
        String audioInput = getFileUrl(params.getAudioPublicId(), "video");

        File trimmedVideo = File.createTempFile("video_trimmed", ".mp4");
        File trimmedAudio = File.createTempFile("audio_trimmed", ".aac");
        File adjustedAudio = File.createTempFile("audio_adjusted", ".aac");
        File mergedOutput = File.createTempFile("merged_output", ".mp4");

        // Trim video
        runProcess(new ProcessBuilder(
                ffmpegPath, "-i", videoInput,
                "-ss", String.valueOf(params.getVideoCutStart()),
                "-to", String.valueOf(params.getVideoCutEnd()),
                "-c", "copy",
                trimmedVideo.getAbsolutePath()));

        // Trim audio
        runProcess(new ProcessBuilder(
                ffmpegPath, "-i", audioInput,
                "-ss", String.valueOf(params.getAudioCutStart()),
                "-to", String.valueOf(params.getAudioCutEnd()),
                "-c", "copy",
                trimmedAudio.getAbsolutePath()));

        // Adjust audio
        String audioFilter = String.format("adelay=%d|%d,volume=%f",
                params.getAudioOffset() * 1000,
                params.getAudioOffset() * 1000,
                params.getAudioVolume() != null ? params.getAudioVolume() : 1.0);

        runProcess(new ProcessBuilder(
                ffmpegPath, "-i", trimmedAudio.getAbsolutePath(),
                "-af", audioFilter,
                "-c:a", "aac",
                adjustedAudio.getAbsolutePath()));

        // Merge video & adjusted audio
        List<String> mergeCmd = new ArrayList<>(List.of(
                ffmpegPath,
                "-i", trimmedVideo.getAbsolutePath(),
                "-i", adjustedAudio.getAbsolutePath(),
                "-c:v", "copy",
                "-c:a", "aac"));

        if (params.getVideoVolume() != null && params.getVideoVolume() != 1.0) {
            mergeCmd.add("-filter:v");
            mergeCmd.add(String.format("volume=%f", params.getVideoVolume()));
        }

        mergeCmd.add(mergedOutput.getAbsolutePath());
        runProcess(new ProcessBuilder(mergeCmd));

        // Upload lên Cloudinary
        String publicId = cloudinaryService.uploadVideo(mergedOutput);

        // Cleanup
        trimmedVideo.delete();
        trimmedAudio.delete();
        adjustedAudio.delete();
        mergedOutput.delete();

        return publicId;
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
