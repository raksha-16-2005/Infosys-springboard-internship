package com.medvault.service;

import com.medvault.exception.FileStorageException;
import com.medvault.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/medical-records}")
    private String uploadDir;

    private Path fileStorageLocation;

    @Autowired
    private FileValidator fileValidator;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file, Long patientId, Long recordId) {
        // Validate file before storing
        fileValidator.validateFile(file);

        String rawFilename = file.getOriginalFilename();
        String originalFilename = fileValidator.sanitizeFilename(rawFilename);
        
        try {
            // Create patient-specific directory
            Path patientDir = this.fileStorageLocation.resolve(patientId.toString());
            Files.createDirectories(patientDir);

            // Generate unique filename
            String fileExtension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                fileExtension = originalFilename.substring(lastDot);
            }
            String fileName = recordId.toString() + fileExtension;
            
            // Store file
            Path targetLocation = patientDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return patientId.toString() + "/" + fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file: " + originalFilename, ex);
        }
    }

    public Path loadFile(String fileUrl) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileUrl).normalize();
            
            // Security check: ensure the resolved path is still within the storage location
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Access denied: File path outside storage directory");
            }
            
            if (!Files.exists(filePath)) {
                throw new FileStorageException("File not found: " + fileUrl);
            }
            
            return filePath;
        } catch (Exception ex) {
            throw new FileStorageException("File not found: " + fileUrl, ex);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileUrl).normalize();
            
            // Security check: ensure the resolved path is still within the storage location
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Access denied: File path outside storage directory");
            }
            
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + fileUrl, ex);
        }
    }
}
