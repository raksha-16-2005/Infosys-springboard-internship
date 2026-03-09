package com.medvault.util;

import com.medvault.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Allowed MIME types for medical records
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            "application/octet-stream", // Fallback for unknown types
            "application/x-msdownload" // Another common fallback
    );

    // Magic bytes for file type validation
    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();
    
    static {
        MAGIC_BYTES.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF
        MAGIC_BYTES.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
    }

    /**
     * Validate file upload for security concerns
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed size of 10MB");
        }

        // Validate filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Filename cannot be empty");
        }

        // Check for path traversal attacks
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new ValidationException("Invalid filename: contains illegal characters");
        }

        // Validate file extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (extension.isEmpty()) {
            throw new ValidationException("File must have an extension");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("File type not allowed: " + contentType);
        }

        // Validate magic bytes (file signature)
        try {
            validateMagicBytes(file, contentType);
        } catch (IOException e) {
            throw new ValidationException("Failed to validate file content", e);
        }
    }

    /**
     * Validate file content by checking magic bytes
     */
    private void validateMagicBytes(MultipartFile file, String contentType) throws IOException {
        byte[] magicBytes = MAGIC_BYTES.get(contentType.toLowerCase());
        if (magicBytes == null) {
            // No magic bytes defined for this type, skip validation
            return;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] fileHeader = new byte[magicBytes.length];
            int bytesRead = is.read(fileHeader);
            
            if (bytesRead < magicBytes.length) {
                throw new ValidationException("File is too small or corrupted");
            }

            for (int i = 0; i < magicBytes.length; i++) {
                if (fileHeader[i] != magicBytes[i]) {
                    throw new ValidationException("File content does not match declared type");
                }
            }
        }
    }

    /**
     * Sanitize filename for storage
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unnamed";
        }

        // Remove any path components
        filename = filename.replaceAll("[\\\\/]", "");
        
        // Remove any characters that are not alphanumeric, dot, dash, or underscore
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Limit length
        if (filename.length() > 255) {
            String extension = getFileExtension(filename);
            int maxNameLength = 255 - extension.length() - 1;
            filename = filename.substring(0, maxNameLength) + "." + extension;
        }

        return filename;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        
        return "";
    }

    /**
     * Validate profile image upload
     */
    public void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Image file cannot be empty");
        }

        // Validate file size (smaller limit for profile images)
        long maxImageSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxImageSize) {
            throw new ValidationException("Image size exceeds maximum allowed size of 5MB");
        }

        // Validate content type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("File must be an image");
        }

        Set<String> allowedImageTypes = Set.of("image/jpeg", "image/jpg", "image/png");
        if (!allowedImageTypes.contains(contentType.toLowerCase())) {
            throw new ValidationException("Only JPEG and PNG images are allowed");
        }

        // Validate filename
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.contains("..") || filename.contains("/") || filename.contains("\\"))) {
            throw new ValidationException("Invalid filename");
        }
    }
}
