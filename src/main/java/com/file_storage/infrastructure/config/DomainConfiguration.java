package com.file_storage.infrastructure.config;

import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.file_storage.domain.service.FileChecksumService;
import com.file_storage.domain.service.FilePathGenerator;

@Configuration
public class DomainConfiguration {
    
    @Bean
    public FileChecksumService fileChecksumService() {
        return new FileChecksumService();
    }
    
    @Bean
    public FilePathGenerator filePathGenerator() {
        return new FilePathGenerator();
    }
    
    @Bean
    public JSch jsch() {
        return new JSch();
    }
}
