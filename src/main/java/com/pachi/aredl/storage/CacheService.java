package com.pachi.aredl.storage;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheService {

    private final Path cacheDirectory;

    public CacheService() {
        this.cacheDirectory = Path.of("data", "cache", "api");
    }

    public boolean exists(String fileName) {
        return Files.exists(getCachePath(fileName));
    }

    public String readJson(String fileName) throws IOException {
        Path path = getCachePath(fileName);
        return Files.readString(path);
    }

    public void writeJson(String fileName, String json) throws IOException {
        Path path = getCachePath(fileName);

        Files.createDirectories(cacheDirectory);
        Files.writeString(path, json);
    }

    private Path getCachePath(String fileName) {
        return cacheDirectory.resolve(fileName);
    }
}
