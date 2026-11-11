
package com.pachi.aredl.service;

import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.changelog.ChangelogDocument;
import com.pachi.aredl.model.changelog.ChangelogEvent;
import com.pachi.aredl.model.changelog.ChangelogRecordState;
import com.pachi.aredl.model.changelog.ChangelogTag;
import com.pachi.aredl.model.country.CountryRank;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.model.player.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangelogProcessorTest {

    private final ChangelogProcessor processor = new ChangelogProcessor();

    // --------------------------------------------------
    // 1. Primera sincronización
    // --------------------------------------------------

    @Test
    void shouldInitializeBaselineWithoutGeneratingEvents() {

        ChangelogDocument document = new ChangelogDocument();

        CountryRecord record = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertTrue(document.isInitialized());
        assertTrue(events.isEmpty());
        assertTrue(document.getEvents().isEmpty());
    }

    @Test
    void shouldRegisterAllRecordsAndPlayersDuringBaseline() {

        ChangelogDocument document = new ChangelogDocument();

        CountryRecord record1 = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        CountryRecord record2 = createRecord(
                "record-2", "player-2", "2024-01-02T10:00:00Z"
        );

        CountryRecord record3 = createRecord(
                "record-3", "player-1", "2024-01-03T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record1),
                createTrackedLevel("level-2", "Level B", 80, record2),
                createTrackedLevel("level-3", "Level C", 60, record3)
        );

        processor.process(document, result);

        assertEquals(3, document.getState().getKnownRecordIds().size());
        assertEquals(2, document.getState().getKnownPlayerIds().size());
        assertEquals(3, document.getState().getKnownRecords().size());

        assertTrue(document.getState().getKnownRecordIds().contains("record-1"));
        assertTrue(document.getState().getKnownRecordIds().contains("record-2"));
        assertTrue(document.getState().getKnownRecordIds().contains("record-3"));
    }

    // --------------------------------------------------
    // 2. Sincronizaciones posteriores
    // --------------------------------------------------

    @Test
    void shouldNotGenerateEventsWhenThereAreNoNewRecords() {

        ChangelogDocument document = initializedDocument();

        CountryRecord record = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        document.getState().getKnownRecordIds().add("record-1");

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertTrue(events.isEmpty());
        assertTrue(document.getEvents().isEmpty());
    }

    @Test
    void shouldNotRegisterTheSameRecordTwice() {

        ChangelogDocument document = initializedDocument();

        document.getState().getKnownRecordIds().add("record-1");

        CountryRecord record = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertTrue(events.isEmpty());
        assertEquals(0, document.getEvents().size());
    }

    // --------------------------------------------------
    // 3. NEW_PLAYER
    // --------------------------------------------------

    @Test
    void shouldDetectNewPlayer() {

        ChangelogDocument document = initializedDocument();

        CountryRecord record = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(1, events.size());

        assertTrue(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.NEW_PLAYER));

        assertFalse(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.NEW_PERSONAL_HARDEST));
    }

    // --------------------------------------------------
    // 4. FIRST_COUNTRY_COMPLETION
    // --------------------------------------------------

    @Test
    void shouldDetectFirstCountryCompletion() {

        ChangelogDocument document = initializedDocument();

        document.getState().getKnownPlayerIds().add("player-1");

        addKnownRecord(
                document,
                "previous-record",
                "previous-level",
                50,
                "player-1",
                "2024-01-01T10:00:00Z"
        );

        CountryRecord record = createRecord(
                "record-1", "player-1", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("new-level", "New Level", 150, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(1, events.size());

        assertTrue(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.FIRST_COUNTRY_COMPLETION));
    }

    @Test
    void shouldNotDetectFirstCompletionWhenLevelAlreadyExists() {

        ChangelogDocument document = initializedDocument();

        document.getState().getKnownPlayerIds().add("player-1");

        addKnownRecord(
                document,
                "previous-record",
                "level-1",
                100,
                "player-1",
                "2024-01-01T10:00:00Z"
        );

        CountryRecord record = createRecord(
                "record-2", "player-1", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-1", "Level A", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(1, events.size());

        assertFalse(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.FIRST_COUNTRY_COMPLETION));
    }

    // --------------------------------------------------
    // 5. NEW_PERSONAL_HARDEST
    // --------------------------------------------------

    @Test
    void shouldDetectNewPersonalHardest() {

        ChangelogDocument document = initializedDocument();

        document.getState().getKnownPlayerIds().add("player-1");

        addKnownRecord(
                document,
                "previous-record",
                "old-level",
                100,
                "player-1",
                "2024-01-01T10:00:00Z"
        );

        CountryRecord record = createRecord(
                "record-2", "player-1", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("new-level", "Harder Level", 50, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(1, events.size());

        assertTrue(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.NEW_PERSONAL_HARDEST));
    }

    @Test
    void shouldNotDetectPersonalHardestWhenLevelIsEasier() {

        ChangelogDocument document = initializedDocument();

        document.getState().getKnownPlayerIds().add("player-1");

        addKnownRecord(
                document,
                "previous-record",
                "old-level",
                50,
                "player-1",
                "2024-01-01T10:00:00Z"
        );

        CountryRecord record = createRecord(
                "record-2", "player-1", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("new-level", "Easier Level", 100, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertFalse(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.NEW_PERSONAL_HARDEST));
    }

    // --------------------------------------------------
    // 6. NEW_COUNTRY_HARDEST
    // --------------------------------------------------

    @Test
    void shouldDetectNewCountryHardest() {

        ChangelogDocument document = initializedDocument();

        addKnownRecord(
                document,
                "previous-record",
                "old-level",
                100,
                "player-1",
                "2024-01-01T10:00:00Z"
        );

        CountryRecord record = createRecord(
                "record-2", "player-2", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel("new-level", "Harder Level", 50, record)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(1, events.size());

        assertTrue(events.getFirst().getSpecialEvents()
                .contains(ChangelogTag.NEW_COUNTRY_HARDEST));
    }

    // --------------------------------------------------
    // 7. Orden cronológico
    // --------------------------------------------------

    @Test
    void shouldProcessNewRecordsChronologically() {

        ChangelogDocument document = initializedDocument();

        CountryRecord laterRecord = createRecord(
                "record-2", "player-1", "2024-02-01T10:00:00Z"
        );

        CountryRecord earlierRecord = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        // El nivel más fácil aparece primero en la entrada.
        TrackerResult result = createTrackerResult(
                createTrackedLevel("level-2", "Harder Level", 50, laterRecord),
                createTrackedLevel("level-1", "Easier Level", 100, earlierRecord)
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(2, events.size());

        // El récord más antiguo debe procesarse primero.
        assertEquals("record-1", events.get(0).getRecordId());
        assertEquals("record-2", events.get(1).getRecordId());

        // Solo el primer récord debe marcar al jugador como nuevo.
        assertTrue(events.get(0).getSpecialEvents()
                .contains(ChangelogTag.NEW_PLAYER));

        assertFalse(events.get(1).getSpecialEvents()
                .contains(ChangelogTag.NEW_PLAYER));

        // El segundo récord supera el hardest personal del primero.
        assertTrue(events.get(1).getSpecialEvents()
                .contains(ChangelogTag.NEW_PERSONAL_HARDEST));
    }

    @Test
    void shouldDetectFirstCountryCompletionOnlyOnceForSameLevel() {

        ChangelogDocument document = initializedDocument();

        CountryRecord earlierRecord = createRecord(
                "record-1", "player-1", "2024-01-01T10:00:00Z"
        );

        CountryRecord laterRecord = createRecord(
                "record-2", "player-2", "2024-02-01T10:00:00Z"
        );

        TrackerResult result = createTrackerResult(
                createTrackedLevel(
                        "level-1",
                        "Level A",
                        100,
                        laterRecord,
                        earlierRecord
                )
        );

        List<ChangelogEvent> events = processor.process(document, result);

        assertEquals(2, events.size());

        assertTrue(events.get(0).getSpecialEvents()
                .contains(ChangelogTag.FIRST_COUNTRY_COMPLETION));

        assertFalse(events.get(1).getSpecialEvents()
                .contains(ChangelogTag.FIRST_COUNTRY_COMPLETION));
    }

    // --------------------------------------------------
    // Métodos auxiliares
    // --------------------------------------------------

    private ChangelogDocument initializedDocument() {
        ChangelogDocument document = new ChangelogDocument();
        document.setInitialized(true);
        return document;
    }

    private void addKnownRecord(
            ChangelogDocument document,
            String recordId,
            String levelId,
            Integer position,
            String playerId,
            String achievedAt
    ) {
        document.getState().getKnownRecordIds().add(recordId);

        if (playerId != null) {
            document.getState().getKnownPlayerIds().add(playerId);
        }

        document.getState().getKnownRecords().add(
                new ChangelogRecordState(
                        recordId,
                        levelId,
                        "Previous Level",
                        position,
                        playerId,
                        achievedAt
                )
        );
    }

    private CountryRecord createRecord(
            String recordId,
            String playerId,
            String achievedAt
    ) {
        CountryRecord record = mock(CountryRecord.class);

        when(record.getId()).thenReturn(recordId);
        when(record.getAchievedAt()).thenReturn(achievedAt);

        if (playerId != null) {
            Player player = mock(Player.class);

            when(player.getId()).thenReturn(playerId);
            when(player.getUsername()).thenReturn("TestUser");
            when(player.getGlobal_name()).thenReturn("Test User");

            when(record.getSubmittedBy()).thenReturn(player);
        }

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

    @Test
    void shouldRecalculateFirstCountryCompletionWhenLateRecordIsDiscovered() {
        ChangelogDocument document = initializedDocument();

        addKnownRecord(
                document,
                "record-may",
                "level-1",
                100,
                "player-1",
                "2024-05-15T12:00:00Z"
        );

        ChangelogEvent previousEvent = new ChangelogEvent(
                "NEW_COUNTRY_RECORD",
                "record-may",
                "level-1",
                "Level 1",
                100,
                "player-1",
                "TestUser",
                "Test User",
                "2024-05-15T12:00:00Z",
                "2024-05-15T12:00:00Z",
                new ArrayList<>(List.of(ChangelogTag.FIRST_COUNTRY_COMPLETION))
        );

        document.getEvents().add(previousEvent);

        CountryRecord lateRecord = createRecord(
                "record-april",
                "player-2",
                "2024-04-10T12:00:00Z"
        );

        TrackerResult trackerResult = createTrackerResult(
                createTrackedLevel(
                        "level-1",
                        "Level 1",
                        100,
                        lateRecord
                )
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        List<ChangelogEvent> newEvents = processor.process(
                document,
                trackerResult
        );

        assertEquals(1, newEvents.size());

        ChangelogEvent lateEvent = newEvents.getFirst();

        assertTrue(lateEvent.getSpecialEvents().contains(
                ChangelogTag.FIRST_COUNTRY_COMPLETION
        ));

        assertFalse(previousEvent.getSpecialEvents().contains(
                ChangelogTag.FIRST_COUNTRY_COMPLETION
        ));
    }

    @Test
    void shouldRecalculateCountryHardestWhenLateRecordIsDiscovered() {
        ChangelogDocument document = initializedDocument();

        addKnownRecord(
                document,
                "record-may",
                "level-1",
                100,
                "player-1",
                "2024-05-15T12:00:00Z"
        );

        ChangelogEvent previousEvent = new ChangelogEvent(
                "NEW_COUNTRY_RECORD",
                "record-may",
                "level-1",
                "Level 1",
                100,
                "player-1",
                "TestUser",
                "Test User",
                "2024-05-15T12:00:00Z",
                "2024-05-15T12:00:00Z",
                new ArrayList<>(List.of(ChangelogTag.NEW_COUNTRY_HARDEST))
        );

        document.getEvents().add(previousEvent);

        CountryRecord lateRecord = createRecord(
                "record-april",
                "player-2",
                "2024-04-10T12:00:00Z"
        );

        TrackerResult trackerResult = createTrackerResult(
                createTrackedLevel(
                        "level-2",
                        "Level 2",
                        50,
                        lateRecord
                )
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        List<ChangelogEvent> newEvents = processor.process(
                document,
                trackerResult
        );

        assertEquals(1, newEvents.size());

        ChangelogEvent lateEvent = newEvents.getFirst();

        assertTrue(lateEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_COUNTRY_HARDEST
        ));

        assertFalse(previousEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_COUNTRY_HARDEST
        ));
    }

    @Test
    void shouldRecalculatePersonalHardestWhenLateRecordIsDiscovered() {
        ChangelogDocument document = initializedDocument();

        addKnownRecord(
                document,
                "record-march",
                "level-3",
                200,
                "player-1",
                "2024-03-10T12:00:00Z"
        );
        addKnownRecord(
                document,
                "record-may",
                "level-1",
                100,
                "player-1",
                "2024-05-15T12:00:00Z"
        );

        ChangelogEvent previousEvent = new ChangelogEvent(
                "NEW_COUNTRY_RECORD",
                "record-may",
                "level-1",
                "Level 1",
                100,
                "player-1",
                "TestUser",
                "Test User",
                "2024-05-15T12:00:00Z",
                "2024-05-15T12:00:00Z",
                new ArrayList<>(List.of(
                        ChangelogTag.NEW_PLAYER,
                        ChangelogTag.NEW_PERSONAL_HARDEST
                ))
        );

        document.getEvents().add(previousEvent);

        CountryRecord lateRecord = createRecord(
                "record-april",
                "player-1",
                "2024-04-10T12:00:00Z"
        );

        TrackerResult trackerResult = createTrackerResult(
                createTrackedLevel(
                        "level-2",
                        "Level 2",
                        50,
                        lateRecord
                )
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        List<ChangelogEvent> newEvents = processor.process(
                document,
                trackerResult
        );

        assertEquals(1, newEvents.size());

        ChangelogEvent lateEvent = newEvents.getFirst();

        assertTrue(lateEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_PERSONAL_HARDEST
        ));

        assertFalse(previousEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_PERSONAL_HARDEST
        ));

        assertFalse(previousEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_PLAYER
        ));
    }

    @Test
    void shouldRecalculateNewPlayerWhenEarlierRecordIsDiscovered() {
        ChangelogDocument document = initializedDocument();

        addKnownRecord(
                document,
                "record-may",
                "level-1",
                100,
                "player-1",
                "2024-05-15T12:00:00Z"
        );

        ChangelogEvent previousEvent = new ChangelogEvent(
                "NEW_COUNTRY_RECORD",
                "record-may",
                "level-1",
                "Level 1",
                100,
                "player-1",
                "TestUser",
                "Test User",
                "2024-05-15T12:00:00Z",
                "2024-05-15T12:00:00Z",
                new ArrayList<>(List.of(ChangelogTag.NEW_PLAYER))
        );

        document.getEvents().add(previousEvent);

        CountryRecord lateRecord = createRecord(
                "record-april",
                "player-1",
                "2024-04-10T12:00:00Z"
        );

        TrackerResult trackerResult = createTrackerResult(
                createTrackedLevel(
                        "level-2",
                        "Level 2",
                        150,
                        lateRecord
                )
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        List<ChangelogEvent> newEvents = processor.process(
                document,
                trackerResult
        );

        assertEquals(1, newEvents.size());

        ChangelogEvent lateEvent = newEvents.getFirst();

        assertTrue(lateEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_PLAYER
        ));

        assertFalse(previousEvent.getSpecialEvents().contains(
                ChangelogTag.NEW_PLAYER
        ));
    }

    @Test
    void shouldNotProcessSameRecordIdTwiceInSameExecution() {
        ChangelogDocument document = initializedDocument();

        CountryRecord duplicatedRecord = createRecord(
                "record-1",
                "player-1",
                "2024-05-15T12:00:00Z"
        );

        TrackedLevel firstLevel = createTrackedLevel(
                "level-1",
                "Level 1",
                100,
                duplicatedRecord
        );

        TrackedLevel secondLevel = createTrackedLevel(
                "level-1",
                "Level 1",
                100,
                duplicatedRecord
        );

        TrackerResult trackerResult = createTrackerResult(
                firstLevel,
                secondLevel
        );

        ChangelogProcessor processor = new ChangelogProcessor();

        List<ChangelogEvent> newEvents = processor.process(
                document,
                trackerResult
        );

        assertEquals(1, newEvents.size());
        assertEquals(1, document.getEvents().size());
        assertEquals(1, document.getState().getKnownRecords().size());
        assertEquals(1, document.getState().getKnownRecordIds().size());
    }
}
