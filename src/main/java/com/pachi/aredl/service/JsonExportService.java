package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.export.LevelExport;
import com.pachi.aredl.model.export.NewRecordsExport;
import com.pachi.aredl.model.export.SummaryExport;
import com.pachi.aredl.model.export.VictorExport;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.model.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
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
        NewRecordsExport emptyExport = new NewRecordsExport(
                Instant.now().toString(),
                List.of()
        );

        export(result, emptyExport);
    }

    public void export(TrackerResult result, NewRecordsExport newRecordsExport) throws IOException {

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

        List<LevelExport> levelsExport = levels.stream()
                .map(this::toLevelExport)
                .toList();

        Files.createDirectories(outputDirectory);

        writeJsonAtomically("summary.json", summary);
        writeJsonAtomically("levels.json", levelsExport);
        writeJsonAtomically("new-records.json", newRecordsExport);
    }


    private LevelExport toLevelExport(TrackedLevel trackedLevel) {
        var level = trackedLevel.getLevel();

        List<VictorExport> victors = trackedLevel.getRecords()
                .stream()
                .map(this::toVictorExport)
                .toList();

        return new LevelExport(
                level.getId(),
                level.getName(),
                level.getPosition(),
                level.getNlwTier(),
                trackedLevel.getDlecPosition(),
                trackedLevel.isCompleted(),
                victors
        );
    }

    private VictorExport toVictorExport(CountryRecord record) {

        Player player = record.getSubmittedBy();

        return new VictorExport(
                record.getId(),
                player != null ? player.getId() : null,
                player != null ? player.getUsername() : null,
                player != null ? player.getGlobal_name() : null,
                player != null ? player.getDiscord_id() : null,
                player != null ? player.getDiscord_avatar() : null,
                record.getCompletionCount(),
                record.getAchievedAt(),
                record.getVideoUrl(),
                record.getHideVideo()
        );
    }

    private void writeJsonAtomically(String filename, Object data)  throws IOException {
        Path target = outputDirectory.resolve(filename);
        Path temporary = outputDirectory.resolve(filename + ".tmp");

        try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(temporary.toFile(), data);

            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
