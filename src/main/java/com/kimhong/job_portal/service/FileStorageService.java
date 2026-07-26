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
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.uploads.upload-resume}")
    private String uploadResumeDir;

    @Value("${file.uploads.upload-image}")
    private String uploadImageDir;

    private void validateFile(MultipartFile file, List<String> allowedType, Long maxSize, String errorMessage){
        if(file == null || file.isEmpty())
            throw new BadRequestException("File cannot be empty");

        if(file.getContentType() == null || !allowedType.contains(file.getContentType()))
            throw new BadRequestException(errorMessage);

        if(file.getSize() > maxSize)
            throw new BadRequestException(errorMessage);
    }

    private String saveToDisk(MultipartFile file, String targetDir){
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if(originalFileName != null && originalFileName.contains(".")){
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID() + extension;

            // Create upload directory
            Path uploadPath = Paths.get(targetDir);

            if(!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            // save file to upload path
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(),uploadPath, StandardCopyOption.REPLACE_EXISTING);

            return uploadPath+"/"+uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String storeResume(MultipartFile file){
        validateFile(file,List.of("application/pdf"),  5 * 1024 * 1024L,"Only PDF files under 5MB are allowed");
        return saveToDisk(file,uploadResumeDir);
    }

    public String storeImage(MultipartFile image){
       List<String> allowedImageType = List.of(
               "image/jpeg",
               "image/png",
               "image/webp"
       );

       validateFile(image,allowedImageType,2 * 1024 * 1024L,"Only JPEG, PNG, or WebP images under 2MB are allowed");
       return saveToDisk(image,uploadImageDir);
    }

    public void deleteFile(String fileUrl){
        try {
            if(fileUrl == null || fileUrl.isBlank()){
                return;
            }
            Path filepath = Paths.get(fileUrl);
            Files.deleteIfExists(filepath);
        } catch (IOException e) {
            throw new RuntimeException("Fail to delete file: "+e.getMessage());
        }
    }

}
