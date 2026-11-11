
package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.changelog.ChangelogDocument;
import com.pachi.aredl.model.changelog.ChangelogEvent;
import com.pachi.aredl.model.changelog.ChangelogTag;
import com.pachi.aredl.model.country.CountryRank;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangelogWorkflowTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistBaselineAndDetectRecordsInNextExecution()
            throws IOException {

        ChangelogService changelogService = new ChangelogService(
                new ObjectMapper(),
                tempDir.resolve("history").resolve("changelog.json")
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        // PRIMERA EJECUCIÓN: establecemos la línea base.

        CountryRecord firstRecord = createRecord(
                "record-1",
                "player-1",
                "2024-03-10T12:00:00Z"
        );

        TrackerResult firstResult = createTrackerResult(
                createTrackedLevel(
                        "level-1",
                        "Level 1",
                        100,
                        firstRecord
                )
        );

        ChangelogDocument firstDocument = changelogService.load();

        List<ChangelogEvent> firstEvents = processor.process(
                firstDocument,
                firstResult
        );

        assertTrue(firstDocument.isInitialized());
        assertTrue(firstEvents.isEmpty());

        changelogService.save(firstDocument);

        // SEGUNDA EJECUCIÓN: aparece un récord nuevo.

        CountryRecord secondRecord = createRecord(
                "record-2",
                "player-1",
                "2024-04-10T12:00:00Z"
        );

        TrackerResult secondResult = createTrackerResult(
                createTrackedLevel(
                        "level-1",
                        "Level 1",
                        100,
                        firstRecord
                ),
                createTrackedLevel(
                        "level-2",
                        "Level 2",
                        50,
                        secondRecord
                )
        );

        ChangelogDocument secondDocument = changelogService.load();

        List<ChangelogEvent> secondEvents = processor.process(
                secondDocument,
                secondResult
        );

        assertEquals(1, secondEvents.size());

        ChangelogEvent event = secondEvents.getFirst();

        assertEquals("record-2", event.getRecordId());

        assertTrue(event.getSpecialEvents().contains(
                ChangelogTag.NEW_PERSONAL_HARDEST
        ));

        assertTrue(event.getSpecialEvents().contains(
                ChangelogTag.NEW_COUNTRY_HARDEST
        ));

        assertTrue(event.getSpecialEvents().contains(
                ChangelogTag.FIRST_COUNTRY_COMPLETION
        ));

        assertFalse(event.getSpecialEvents().contains(
                ChangelogTag.NEW_PLAYER
        ));

        // Guardamos y volvemos a cargar el historial.

        changelogService.save(secondDocument);

        ChangelogDocument finalDocument = changelogService.load();

        assertTrue(finalDocument.isInitialized());

        assertEquals(2,
                finalDocument.getState().getKnownRecordIds().size());

        assertEquals(1, finalDocument.getEvents().size());

        assertEquals(
                "record-2",
                finalDocument.getEvents().getFirst().getRecordId()
        );
    }

    private CountryRecord createRecord(
            String recordId,
            String playerId,
            String achievedAt
    ) {
        CountryRecord record = mock(CountryRecord.class);

        Player player = mock(Player.class);

        when(record.getId()).thenReturn(recordId);
        when(record.getAchievedAt()).thenReturn(achievedAt);
        when(record.getSubmittedBy()).thenReturn(player);

        when(player.getId()).thenReturn(playerId);
        when(player.getUsername()).thenReturn("TestUser");
        when(player.getGlobal_name()).thenReturn("Test User");

        return record;
    }

    private TrackedLevel createTrackedLevel(
            String levelId,
            String levelName,
            Integer position,
            CountryRecord... records
    ) {
        Level level = mock(Level.class);

        when(level.getId()).thenReturn(levelId);
        when(level.getName()).thenReturn(levelName);
        when(level.getPosition()).thenReturn(position);

        TrackedLevel trackedLevel = mock(TrackedLevel.class);

        when(trackedLevel.getLevel()).thenReturn(level);
        when(trackedLevel.getRecords()).thenReturn(List.of(records));

        return trackedLevel;
    }

    private TrackerResult createTrackerResult(TrackedLevel... levels) {
        return new TrackerResult(
                mock(CountryRank.class),
                List.of(levels)
        );
    }
}
