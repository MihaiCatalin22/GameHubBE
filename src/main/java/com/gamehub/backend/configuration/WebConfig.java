package com.gamehub.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String fileUploadDir;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + fileUploadDir.replace("\\", "/");
        if (!resourcePath.endsWith("/")) {
            resourcePath += "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourcePath);
    }
}
