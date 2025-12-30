package com.file_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileStorageApplication {

	static void main(String[] args) {
		SpringApplication.run(FileStorageApplication.class, args);
	}

}
