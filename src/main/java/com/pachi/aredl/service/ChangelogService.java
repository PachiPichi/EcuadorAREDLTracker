package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.changelog.ChangelogDocument;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ChangelogService {

    private final ObjectMapper objectMapper;
    private final Path changelogPath;

    public ChangelogService() {
        this(new ObjectMapper(), Path.of("data", "history", "changelog.json"));
    }

    public ChangelogService(ObjectMapper objectMapper, Path changelogPath) {
        this.objectMapper = objectMapper;
        this.changelogPath = changelogPath;
    }

    public ChangelogDocument load() throws IOException {
        if (Files.notExists(changelogPath)) {
            return new ChangelogDocument();
        }

        return objectMapper.readValue(
                changelogPath.toFile(),
                ChangelogDocument.class
        );
    }

    public void save(ChangelogDocument document) throws IOException {
        Path directory = changelogPath.getParent();

        Files.createDirectories(directory);

        Path temporaryPath = changelogPath.resolveSibling(
                changelogPath.getFileName() + ".tmp"
        );

        try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(temporaryPath.toFile(), document);

            try {
                Files.move(
                        temporaryPath,
                        changelogPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryPath,
                        changelogPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
    }
}
