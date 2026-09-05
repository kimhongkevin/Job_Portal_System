package com.kimhong.job_portal.service;

import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket.resumes}")
    private String resumeBucket;

    @Value("${supabase.bucket.logos}")
    private String logoBucket;

    private final RestTemplate restTemplate;

    private void validateFile(MultipartFile file, List<String> allowedType, Long maxSize, String errorMessage) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("File cannot be empty");

        if (file.getContentType() == null || !allowedType.contains(file.getContentType()))
            throw new BadRequestException(errorMessage);

        if (file.getSize() > maxSize)
            throw new BadRequestException(errorMessage);
    }

    public String getPublicUrl(String bucket, String fileName) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    public String getSignedUrl(String bucket, String fileName, int expiresInSecond) {
        String signUrl = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"expiresIn\": " + expiresInSecond + "}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    signUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            Object signedPath = response.getBody() != null ? response.getBody().get("signedURL") : null;
            if (signedPath == null) {
                throw new StorageException("Supabase did not return a signed URL for " + bucket + "/" + fileName);
            }
            return supabaseUrl + signedPath;

        } catch (HttpStatusCodeException e) {
            log.error("Supabase sign request failed for {}/{}: {} - {}",
                    bucket, fileName, e.getStatusCode(), e.getResponseBodyAsString());
            throw new StorageException("Failed to generate signed URL: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating signed URL for {}/{}", bucket, fileName, e);
            throw new StorageException("Failed to generate signed URL", e);
        }
    }

    public String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    private String uploadFile(MultipartFile file, String bucket) {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String uniqueFileName = UUID.randomUUID() + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + uniqueFileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"
        ));

        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.postForEntity(uploadUrl, entity, String.class);
            return getPublicUrl(bucket, uniqueFileName);

        } catch (IOException e) {
            log.error("Failed to read file bytes for upload to {}", bucket, e);
            throw new StorageException("Could not read file for upload", e);
        } catch (HttpStatusCodeException e) {
            log.error("Supabase upload failed for bucket {}: {} - {}",
                    bucket, e.getStatusCode(), e.getResponseBodyAsString());
            throw new StorageException("Failed to upload file: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file to bucket {}", bucket, e);
            throw new StorageException("Failed to upload file", e);
        }
    }

    public String uploadResume(MultipartFile file) {
        validateFile(file, List.of("application/pdf"), 5 * 1024 * 1024L, "Only PDF files under 5MB are allowed");
        return uploadFile(file, resumeBucket);
    }

    public String uploadLogo(MultipartFile file) {
        List<String> allowedImageType = List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        );

        validateFile(file, allowedImageType, 2 * 1024 * 1024L, "Only JPEG, PNG, or Webp images under 2MB are allowed");
        return uploadFile(file, logoBucket);
    }

    public void deleteFile(String fileUrl, String bucket) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String fileName = extractFileName(fileUrl);
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("apikey", serviceKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            log.error("Supabase delete failed for bucket {} file {}: {} - {}",
                    bucket, fileName, e.getStatusCode(), e.getResponseBodyAsString());
            throw new StorageException("Failed to delete file: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error deleting file {} from bucket {}", fileName, bucket, e);
            throw new StorageException("Failed to delete file", e);
        }
    }

    public void deleteResume(String fileUrl) {
        deleteFile(fileUrl, resumeBucket);
    }

    public void deleteLogo(String fileUrl) {
        deleteFile(fileUrl, logoBucket);
    }
}