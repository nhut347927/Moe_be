package com.moe.socialnetwork.api.services;

import java.io.File;
import java.io.IOException;

import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO.FFmpegMergeParams;

public interface IFFmpegService {
    File extractAudioFromVideo(File videoFile) throws IOException;
    File mergeVideoWithAudio(File video, File audio) throws IOException;
    File downloadFileFromCloudinary(String publicId, String fileName, String resourceType);
    String mergeAndUpload(FFmpegMergeParams params) throws IOException, InterruptedException;
}
