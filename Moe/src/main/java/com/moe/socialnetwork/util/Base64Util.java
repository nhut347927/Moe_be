package com.moe.socialnetwork.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Utility class for converting between Base64 and file types.
 */
public class Base64Util {

    /**
     * Convert a Base64-encoded string into a File and save it to the specified
     * path.
     *
     * @param base64     The Base64 string, optionally with a data URI header
     * @param outputPath The full output path including filename (e.g.,
     *                   /tmp/image.png)
     * @return The created File object
     * @throws IOException If writing to file fails
     */
    public static File base64ToFile(String base64, String outputPath) throws IOException {
        String cleanBase64 = cleanBase64Header(base64);
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
        File outputFile = new File(outputPath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decodedBytes);
        }
        return outputFile;
    }

    /**
     * Convert a File to a Base64-encoded string.
     *
     * @param file The input file to be encoded
     * @return The Base64 string representation of the file
     * @throws IOException If reading the file fails
     */
    public static String fileToBase64(File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Convert a MultipartFile (typically from an HTTP upload) to a Base64 string.
     *
     * @param multipartFile The uploaded file from client
     * @return The Base64 string representation of the file
     * @throws IOException If reading file bytes fails
     */
    public static String multipartFileToBase64(MultipartFile multipartFile) throws IOException {
        byte[] fileBytes = multipartFile.getBytes();
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Convert a Base64-encoded string into a Spring MultipartFile.
     *
     * @param base64      The Base64-encoded string
     * @param fileName    The desired filename
     * @param contentType MIME type of the file (e.g., image/png)
     * @return A MultipartFile that can be used like an uploaded file
     */
    public static MultipartFile base64ToMultipartFile(String base64, String fileName, String contentType) {
        String cleanBase64 = cleanBase64Header(base64);
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
        return new MultipartFile() {
            @Override
            public String getName() {
            return fileName;
            }

            @Override
            public String getOriginalFilename() {
            return fileName;
            }

            @Override
            public String getContentType() {
            return contentType;
            }

            @Override
            public boolean isEmpty() {
            return decodedBytes.length == 0;
            }

            @Override
            public long getSize() {
            return decodedBytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
            return decodedBytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(decodedBytes);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(decodedBytes);
            }
            }
        };
    }

    /**
     * Strip the data URI prefix from a Base64 string if present (e.g.,
     * "data:image/png;base64,...").
     *
     * @param base64 The original Base64 string
     * @return The cleaned Base64 string without header
     */
    public static String cleanBase64Header(String base64) {
        if (base64.contains(",")) {
            return base64.substring(base64.indexOf(",") + 1);
        }
        return base64;
    }
}
