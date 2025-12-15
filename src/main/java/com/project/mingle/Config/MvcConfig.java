package com.project.mingle.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/uploads/**' URL 패턴으로 요청이 올 경우,
        // 'file:uploads/' 디렉토리에서 리소스를 찾아서 제공합니다.
        // 이를 통해 서버에 저장된 프로필 이미지를 브라우저에서 접근할 수 있게 됩니다.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}