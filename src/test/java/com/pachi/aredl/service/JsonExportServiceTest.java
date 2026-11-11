
package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.country.CountryRank;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonExportServiceTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private JsonExportService exportService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        exportService = new JsonExportService(
                objectMapper,
                tempDir
        );
    }

    @Test
    void shouldExportSummaryWithCorrectStatistics() throws IOException {

        CountryRank rank = createTestRank();

        TrackedLevel completed1 = createTestTrackedLevel(
                "level-1",
                "Level One",
                1,
                List.of(createTestRecord("record-1", "Player One", 1)),
                1
        );

        TrackedLevel completed2 = createTestTrackedLevel(
                "level-2",
                "Level Two",
                2,
                List.of(createTestRecord("record-2", "Player Two", 1)),
                2
        );

        TrackedLevel pending = createTestTrackedLevel(
                "level-3",
                "Level Three",
                3,
                List.of(),
                null
        );

        TrackerResult result = new TrackerResult(
                rank,
                List.of(completed1, completed2, pending)
        );

        exportService.export(result);

        Path summaryPath = tempDir.resolve("summary.json");

        assertTrue(Files.exists(summaryPath));

        JsonNode summary = objectMapper.readTree(summaryPath.toFile());

        assertEquals(70, summary.get("rank").asInt());
        assertEquals(61, summary.get("extremesRank").asInt());
        assertEquals(83074, summary.get("levelPoints").asInt());
        assertEquals(251, summary.get("extremes").asInt());

        assertEquals(3, summary.get("totalLevels").asInt());
        assertEquals(2, summary.get("completedLevels").asInt());
        assertEquals(1, summary.get("pendingLevels").asInt());

        assertEquals(
                summary.get("totalLevels").asInt(),
                summary.get("completedLevels").asInt()
                        + summary.get("pendingLevels").asInt()
        );
    }

    @Test
    void shouldExportLevelsWithAllTheirVictors() throws IOException {

        TrackedLevel trackedLevel = createTestTrackedLevel(
                "level-1",
                "Level One",
                1501,
                List.of(
                        createTestRecord("record-1", "Player One", 2),
                        createTestRecord("record-2", "Player Two", 2)
                ),
                1
        );

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(trackedLevel)
        );

        exportService.export(result);

        Path levelsPath = tempDir.resolve("levels.json");

        assertTrue(Files.exists(levelsPath));

        JsonNode levels = objectMapper.readTree(levelsPath.toFile());

        assertEquals(1, levels.size());

        JsonNode level = levels.get(0);

        assertEquals("level-1", level.get("id").asText());
        assertEquals("Level One", level.get("name").asText());
        assertEquals(1501, level.get("position").asInt());
        assertEquals("T5", level.get("nlw_tier").asText());
        assertEquals(1, level.get("dlec_position").asInt());
        assertTrue(level.get("completed").asBoolean());

        JsonNode victors = level.get("victors");

        assertEquals(2, victors.size());

        assertEquals("record-1", victors.get(0).get("record_id").asText());
        assertEquals("Player One", victors.get(0).get("username").asText());

        assertEquals("record-2", victors.get(1).get("record_id").asText());
        assertEquals("Player Two", victors.get(1).get("username").asText());
    }

    @Test
    void shouldExportPendingLevelsWithoutVictors() throws IOException {

        TrackedLevel pending = createTestTrackedLevel(
                "level-1",
                "Pending Level",
                2000,
                List.of(),
                null
        );

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(pending)
        );

        exportService.export(result);

        JsonNode levels = objectMapper.readTree(
                tempDir.resolve("levels.json").toFile()
        );

        JsonNode level = levels.get(0);

        assertFalse(level.get("completed").asBoolean());
        assertEquals(0, level.get("victors").size());
        assertTrue(level.get("dlec_position").isNull());
    }

    @Test
    void shouldExportVictorWithAllAvailableFields() throws IOException {

        CountryRecord record = createTestRecord(
                "record-1",
                "Player One",
                2
        );

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(createTestTrackedLevel(
                        "level-1",
                        "Level One",
                        1,
                        List.of(record),
                        1
                ))
        );

        exportService.export(result);

        JsonNode levels = objectMapper.readTree(
                tempDir.resolve("levels.json").toFile()
        );

        JsonNode victor = levels.get(0)
                .get("victors")
                .get(0);

        assertEquals("record-1", victor.get("record_id").asText());
        assertEquals("player-1", victor.get("player_id").asText());
        assertEquals("Player One", victor.get("username").asText());
        assertEquals("Visible Player", victor.get("global_name").asText());

        assertEquals("discord-1", victor.get("discord_id").asText());
        assertEquals("avatar-1", victor.get("discord_avatar").asText());

        assertEquals(2, victor.get("completion_count").asInt());
        assertEquals("2026-11-11T10:00:00", victor.get("achieved_at").asText());
        assertEquals("https://example.com/video", victor.get("video_url").asText());
        assertFalse(victor.get("hide_video").asBoolean());
    }

    @Test
    void shouldHandleVictorWithoutPlayer() throws IOException {

        CountryRecord record = new CountryRecord();

        record.setId("record-1");
        record.setCompletionCount(1);

        TrackedLevel trackedLevel = createTestTrackedLevel(
                "level-1",
                "Level One",
                1,
                List.of(record),
                1
        );

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(trackedLevel)
        );

        assertDoesNotThrow(() -> exportService.export(result));

        JsonNode levels = objectMapper.readTree(
                tempDir.resolve("levels.json").toFile()
        );

        JsonNode victor = levels.get(0)
                .get("victors")
                .get(0);

        assertEquals("record-1", victor.get("record_id").asText());
        assertTrue(victor.get("player_id").isNull());
        assertTrue(victor.get("username").isNull());
        assertTrue(victor.get("global_name").isNull());
    }

    @Test
    void shouldReplacePreviouslyExportedFiles() throws IOException {

        Files.createDirectories(tempDir);

        Files.writeString(
                tempDir.resolve("summary.json"),
                "{\"old\": true}"
        );

        Files.writeString(
                tempDir.resolve("levels.json"),
                "{\"old\": true}"
        );

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(createTestTrackedLevel(
                        "level-1",
                        "New Level",
                        1,
                        List.of(),
                        null
                ))
        );

        exportService.export(result);

        JsonNode summary = objectMapper.readTree(
                tempDir.resolve("summary.json").toFile()
        );

        JsonNode levels = objectMapper.readTree(
                tempDir.resolve("levels.json").toFile()
        );

        assertFalse(summary.has("old"));
        assertTrue(summary.has("rank"));

        assertFalse(levels.isObject());
        assertEquals("New Level", levels.get(0).get("name").asText());
    }

    @Test
    void shouldNotLeaveTemporaryFilesAfterSuccessfulExport() throws IOException {

        TrackerResult result = new TrackerResult(
                createTestRank(),
                List.of(createTestTrackedLevel(
                        "level-1",
                        "Level One",
                        1,
                        List.of(),
                        null
                ))
        );

        exportService.export(result);

        assertFalse(Files.exists(tempDir.resolve("summary.json.tmp")));
        assertFalse(Files.exists(tempDir.resolve("levels.json.tmp")));
    }

    // --------------------------------------------------
    // Métodos auxiliares para construir los datos de prueba
    // --------------------------------------------------

    private CountryRank createTestRank() {

        CountryRank rank = new CountryRank();

        rank.setRank(70);
        rank.setExtremesRank(61);
        rank.setLevelPoints(83074);
        rank.setExtremes(251);

        return rank;
    }

    private TrackedLevel createTestTrackedLevel(
            String id,
            String name,
            int position,
            List<CountryRecord> records,
            Integer dlecPosition
    ) {

        Level level = new Level();

        level.setId(id);
        level.setName(name);
        level.setPosition(position);
        level.setNlwTier("T5");

        return new TrackedLevel(
                level,
                records,
                dlecPosition
        );
    }

    private CountryRecord createTestRecord(
            String id,
            String username,
            int completionCount
    ) {

        Player player = new Player();

        player.setId("player-1");
        player.setUsername(username);
        player.setGlobal_name("Visible Player");
        player.setDiscord_id("discord-1");
        player.setDiscord_avatar("avatar-1");

        CountryRecord record = new CountryRecord();

        record.setId(id);
        record.setSubmittedBy(player);
        record.setCompletionCount(completionCount);
        record.setAchievedAt("2026-11-11T10:00:00");
        record.setVideoUrl("https://example.com/video");
        record.setHideVideo(false);

        return record;
    }
}
