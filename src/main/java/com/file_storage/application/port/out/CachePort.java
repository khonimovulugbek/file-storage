package com.file_storage.application.port.out;

import java.time.Duration;

public interface CachePort {
    void set(String key, Object value, Duration duration);
    Object get(String key);
    void delete(String key);
    void deletePattern(String pattern);
}
