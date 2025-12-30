package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.UserId;

import java.time.LocalDate;

public class FilePathGenerator {

    public String generateBasePath(UserId<?> ownerId) {
        int day = LocalDate.now().getDayOfMonth();
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        String basePath = ownerId == null ? "files" : "users/" + ownerId.value();
        return year + "/" + month + "/" + day + "/" + basePath;
    }

    public String generateBucketName() {
        return "files-" + LocalDate.now().getYear() + "-" +
                String.format("%02d", LocalDate.now().getMonthValue());
    }
}
