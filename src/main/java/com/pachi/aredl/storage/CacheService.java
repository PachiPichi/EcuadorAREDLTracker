package com.pachi.aredl.storage;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

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

        if (!Files.exists(path)) {
            throw new IOException("Archivo no encontrado: " + path);
        }

        if (Files.size(path) == 0) {
            throw new IOException(
                    "El archivo de caché está vacío: " +  path
            );
        }

        String json = Files.readString(path);

        if (json.isBlank()) {
            throw new IOException(
                    "El archivo de caché no contiene JSON válido: " + path
            );
        }

        return json;
    }

    public void writeJson(String fileName, String json) throws IOException {
        Path path = getCachePath(fileName);

        Files.createDirectories(path.getParent());

        Path temporaryPath = Files.createTempFile(
                path.getParent(),
                path.getFileName().toString(),
                ".tmp"
        );
        try {
        Files.writeString(temporaryPath, json);
            try {
                Files.move(
                        temporaryPath,
                        path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryPath,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
    }

    private Path getCachePath(String fileName) {
        return cacheDirectory.resolve(fileName);
    }
}
