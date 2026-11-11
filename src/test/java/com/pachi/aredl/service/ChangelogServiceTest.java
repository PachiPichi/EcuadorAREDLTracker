
package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.changelog.ChangelogDocument;
import com.pachi.aredl.model.changelog.ChangelogEvent;
import com.pachi.aredl.model.changelog.ChangelogRecordState;
import com.pachi.aredl.model.changelog.ChangelogTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChangelogServiceTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private ChangelogService changelogService;
    private Path changelogPath;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        changelogPath = tempDir
                .resolve("history")
                .resolve("changelog.json");

        changelogService = new ChangelogService(
                objectMapper,
                changelogPath
        );
    }

    @Test
    void shouldReturnEmptyDocumentWhenChangelogDoesNotExist()
            throws IOException {

        ChangelogDocument document = changelogService.load();

        assertNotNull(document);
        assertFalse(document.isInitialized());
        assertNotNull(document.getEvents());
        assertTrue(document.getEvents().isEmpty());

        assertNotNull(document.getState());
        assertTrue(document.getState().getKnownRecordIds().isEmpty());
        assertTrue(document.getState().getKnownPlayerIds().isEmpty());
        assertTrue(document.getState().getKnownRecords().isEmpty());
    }

    @Test
    void shouldSaveAndLoadChangelogCorrectly() throws IOException {

        ChangelogDocument document = new ChangelogDocument();
        document.setInitialized(true);

        document.getState().getKnownRecordIds().add("record-1");
        document.getState().getKnownPlayerIds().add("player-1");

        document.getState().getKnownRecords().add(
                new ChangelogRecordState(
                        "record-1",
                        "level-1",
                        "Test Level",
                        100,
                        "player-1",
                        "2024-05-15T12:00:00Z"
                )
        );

        document.getEvents().add(
                new ChangelogEvent(
                        "NEW_COUNTRY_RECORD",
                        "record-1",
                        "level-1",
                        "Test Level",
                        100,
                        "player-1",
                        "TestUser",
                        "Test User",
                        "2024-05-15T12:00:00Z",
                        "2024-05-16T12:00:00Z",
                        new ArrayList<>(List.of(
                                ChangelogTag.NEW_PLAYER,
                                ChangelogTag.FIRST_COUNTRY_COMPLETION
                        ))
                )
        );

        changelogService.save(document);

        assertTrue(Files.exists(changelogPath));

        ChangelogDocument loadedDocument = changelogService.load();

        assertTrue(loadedDocument.isInitialized());

        assertEquals(
                document.getState().getKnownRecordIds(),
                loadedDocument.getState().getKnownRecordIds()
        );

        assertEquals(
                document.getState().getKnownPlayerIds(),
                loadedDocument.getState().getKnownPlayerIds()
        );

        assertEquals(
                document.getState().getKnownRecords().size(),
                loadedDocument.getState().getKnownRecords().size()
        );

        assertEquals(1, loadedDocument.getEvents().size());

        ChangelogEvent event = loadedDocument.getEvents().getFirst();

        assertEquals("record-1", event.getRecordId());
        assertEquals("level-1", event.getLevelId());
        assertEquals("TestUser", event.getUsername());

        assertEquals(
                List.of(
                        ChangelogTag.NEW_PLAYER,
                        ChangelogTag.FIRST_COUNTRY_COMPLETION
                ),
                event.getSpecialEvents()
        );

        // Comprobamos también los nombres de las propiedades JSON.
        JsonNode json = objectMapper.readTree(changelogPath.toFile());

        assertTrue(json.has("schema_version"));
        assertTrue(json.has("initialized"));
        assertTrue(json.has("events"));
        assertTrue(json.has("state"));

        assertTrue(json.get("state").has("known_record_ids"));
        assertTrue(json.get("state").has("known_player_ids"));
        assertTrue(json.get("state").has("known_records"));

        assertTrue(json.get("events").get(0).has("record_id"));
        assertTrue(json.get("events").get(0).has("special_events"));
    }

    @Test
    void shouldNotOverwriteCorruptedChangelog() throws IOException {

        Files.createDirectories(changelogPath.getParent());

        String corruptedJson = "{ invalid json";

        Files.writeString(changelogPath, corruptedJson);

        assertThrows(
                IOException.class,
                () -> changelogService.load()
        );

        assertEquals(
                corruptedJson,
                Files.readString(changelogPath)
        );
    }
}
