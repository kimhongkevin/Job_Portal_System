package com.kimhong.job_portal.service;

import com.kimhong.job_portal.exception.BadRequestException;
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

    public String storeFile(MultipartFile file) {
        try {
            // Check whether file is empty
            if(file.isEmpty())
                throw new BadRequestException("File is empty");

            // Validate file is PDF
            String contentType = file.getContentType();
            if(contentType == null || !contentType.equals("application/pdf"))
                throw new BadRequestException("Only PDF files are allowed");

            // Validate file size
            if(file.getSize() > 5*1024*1024)
                throw new BadRequestException("File size must be less than 5MB");

            // Generate unique file names, avoid duplicated file name
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString()+extension;

            // Create upload directory
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            //Save file to path
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

            return uploadDir+"/"+uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void deleteFile(String fileUrl){
        if(fileUrl == null || fileUrl.isBlank()){
            return;
        }
        try {
            Path filePath = Paths.get(fileUrl);
            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
