package com.kimhong.job_portal.service;

import com.kimhong.job_portal.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final RestTemplate restTemplate = new RestTemplate();

    private void validateFile(MultipartFile file, List<String> allowedType, Long maxSize, String errorMessage){
        if(file == null || file.isEmpty())
            throw new BadRequestException("File cannot be empty");

        if(file.getContentType() == null || !allowedType.contains(file.getContentType()))
            throw new BadRequestException(errorMessage);

        if(file.getSize() > maxSize)
            throw new BadRequestException(errorMessage);
    }

    public String getPublicUrl(String bucket, String fileName){
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    public String getSignedUrl(String bucket, String fileName, int expiresInSecond){
        try{
            String supabaseUrlEndpoint = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","Bearer "+serviceKey);
            headers.set("apikey",serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"expiresIn\": " + expiresInSecond + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(supabaseUrlEndpoint,entity,Map.class);

            if (response.getBody() != null) {
                String signedPath = (String) response.getBody()
                        .get("signedURL");
                return supabaseUrl + signedPath;
            }


        } catch (Exception e) {
            System.err.println(
                    "Failed to generate signed URL: " + e.getMessage()
            );
        }
        return null;
    }

    public String extractFileName(String fileUrl){
        if (fileUrl == null || fileUrl.isBlank()) return null;
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    private String uploadFile(MultipartFile file,String bucket){
        try{

            String originalFileName = file.getOriginalFilename();

            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

            String uniqueFileName = UUID.randomUUID() + extension;

            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + uniqueFileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","Bearer "+serviceKey);
            headers.set("apikey",serviceKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null
                            ? file.getContentType()
                            : "application/octet-stream"
            ));

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(),headers);

            restTemplate.postForEntity(uploadUrl,entity,String.class);

            return getPublicUrl(bucket,uniqueFileName);


        } catch (Exception e) {
            throw new RuntimeException("Fail to upload file: "+e.getMessage());
        }
    }

    public String uploadResume(MultipartFile file){
        validateFile(file,List.of("application/pdf"),5 * 1024 * 1024L,"Only PDF files under 5MB are allowed");
        return uploadFile(file,resumeBucket);
    }

    public String uploadLogo(MultipartFile file){
        List<String> allowedImageType = List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        );


        validateFile(file,allowedImageType,2*1024*1024L,"Only JPEG, PNG, or Webp images under 2MB are allowed");

        return uploadFile(file,logoBucket);
    }

    public void deleteFile(String fileUrl,String bucket){
        if(fileUrl == null || fileUrl.isBlank()) return;

        try{
            String fileName = extractFileName(fileUrl);

            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","Bearer " + serviceKey);
            headers.set("apikey",serviceKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    entity,
                    void.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Fail to delete file: " + e.getMessage());
        }
    }
}
