package com.kimhong.job_portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Value("${file.uploads.upload-resume}")
    private String uploadResumeDir;

    @Value("${file.uploads.upload-image}")
    private String uploadImageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resumePath = Paths.get(uploadResumeDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/resumes/**")
                .addResourceLocations(resumePath.endsWith("/") ? resumePath : resumePath + "/" );

        String imagePath = Paths.get(uploadImageDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(imagePath.endsWith("/") ? imagePath : imagePath + "/");

    }
}
