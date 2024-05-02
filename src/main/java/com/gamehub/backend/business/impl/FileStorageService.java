package com.gamehub.backend.business.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file, Long userId) throws IOException {
        String fileName = userId + "_profile_pic.jpg";
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public String replaceFile(MultipartFile file, Long userId, String oldFileName) throws IOException {
        Path oldFilePath = Paths.get(uploadDir).resolve(oldFileName);
        Files.deleteIfExists(oldFilePath);

        String newFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path newFilePath = Paths.get(uploadDir).resolve(newFileName);
        Files.copy(file.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }
}
