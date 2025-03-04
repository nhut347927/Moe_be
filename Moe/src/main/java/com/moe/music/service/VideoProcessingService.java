package com.moe.music.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public class VideoProcessingService {

    /**
     * Tách âm thanh từ video.
     * Lệnh FFmpeg: ffmpeg -i input.mp4 -q:a 0 -map a output.mp3
     *
     * @param videoPath Đường dẫn đến file video
     * @return Đường dẫn đến file audio tách được
     * @throws IOException, InterruptedException
     */
    public Path extractAudio(Path videoPath) throws IOException, InterruptedException {
        // Tạo file tạm cho audio (.mp3)
        Path outputAudioFile = Files.createTempFile("audio", ".mp3");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath.toString(),
                "-q:a", "0",
                "-map", "a",
                outputAudioFile.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed to extract audio, exit code " + exitCode);
        }
        return outputAudioFile;
    }

    /**
     * Ghép âm thanh vào video.
     * Lệnh FFmpeg: ffmpeg -i video.mp4 -i audio.mp3 -c:v copy -c:a aac -strict experimental output.mp4
     *
     * @param videoPath Đường dẫn file video gốc
     * @param audioPath Đường dẫn file audio cần ghép
     * @return Đường dẫn đến file video đã ghép
     * @throws IOException, InterruptedException
     */
    public Path mergeAudio(Path videoPath, Path audioPath) throws IOException, InterruptedException {
        // Tạo file tạm cho video đã ghép (.mp4)
        Path outputVideoFile = Files.createTempFile("merged", ".mp4");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath.toString(),
                "-i", audioPath.toString(),
                "-c:v", "copy",
                "-c:a", "aac",
                "-strict", "experimental",
                outputVideoFile.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed to merge audio and video, exit code " + exitCode);
        }
        return outputVideoFile;
    }
}
