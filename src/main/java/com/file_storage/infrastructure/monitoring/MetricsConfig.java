package com.file_storage.infrastructure.monitoring;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Timer.Builder fileUploadTimer(MeterRegistry registry) {
        return Timer.builder("file.upload.duration")
                .description("Time taken to upload files")
                .tag("service", "file-storage");
    }

    @Bean
    public Timer.Builder fileDownloadTimer(MeterRegistry registry) {
        return Timer.builder("file.download.duration")
                .description("Time taken to download files")
                .tag("service", "file-storage");
    }
}
