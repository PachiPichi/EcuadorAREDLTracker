package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.export.SummaryExport;
import com.pachi.aredl.model.level.TrackedLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class JsonExportService {

    private final ObjectMapper objectMapper;
    private final Path outputDirectory;

    public JsonExportService(){
        this(new ObjectMapper(), Path.of("data", "current"));
    }

    public JsonExportService(ObjectMapper objectMapper, Path outputDirectory) {
        this.objectMapper = objectMapper;
        this.outputDirectory = outputDirectory;
    }

    public void export(TrackerResult result) throws IOException {

        List<TrackedLevel> levels = result.getTrackedLevels();

        long completedLevels = levels.stream()
                .filter(TrackedLevel::isCompleted)
                .count();

        long pendingLevels = levels.size() - completedLevels;

        var rank = result.getRank();

        SummaryExport summary = new SummaryExport(
                rank.getRank(),
                rank.getExtremesRank(),
                rank.getLevelPoints(),
                rank.getExtremes(),
                levels.size(),
                completedLevels,
                pendingLevels
        );

        Files.createDirectories(outputDirectory);

        Path target = outputDirectory.resolve("summary.json");
        Path temporary = outputDirectory.resolve("summary.json.tmp");

        try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(temporary.toFile(), summary);

            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            throw new IOException(
                    "Error al exportar summary.json. "
                            + "Archivo temporal: " + temporary
                            + ". Archivo destino: " + target,
                    e
            );
        }
    }
}
