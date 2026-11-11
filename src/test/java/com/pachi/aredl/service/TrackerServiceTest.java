
package com.pachi.aredl.service;

import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.LevelRecordsCache;
import com.pachi.aredl.model.level.TrackedLevel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrackerServiceTest {

    @Test
    void shouldFetchCountryDataOnlyOnce() throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));
        when(countryService.getCatchedCountryData()).thenReturn(null);

        CountryData countryData = createTestCountryData(
                createTestRecord("record-1", 1)
        );

        when(countryService.fetchCountryData()).thenReturn(countryData);

        TrackerService tracker = new TrackerService(levelService, countryService);

        tracker.getTrackedLevels();

        verify(countryService, times(1)).getCatchedCountryData();
        verify(countryService, times(1)).fetchCountryData();
        verify(countryService, times(1)).saveCountryData(countryData);
    }

    @Test
    void shouldUseCachedLevelsWhenApiFails() throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level cachedLevel = createTestLevel();

        when(levelService.fetchLevels())
                .thenThrow(new IOException("API unavailable"));

        when(levelService.getCachedLevels())
                .thenReturn(List.of(cachedLevel));

        when(countryService.getCatchedCountryData()).thenReturn(null);

        CountryData countryData = createTestCountryData();

        when(countryService.fetchCountryData()).thenReturn(countryData);

        TrackerService tracker = new TrackerService(levelService, countryService);

        tracker.getTrackedLevels();

        verify(levelService, times(1)).fetchLevels();
        verify(levelService, times(1)).getCachedLevels();

        verify(levelService, never()).saveLevels(anyList());

        verify(countryService, times(1)).fetchCountryData();
        verify(countryService, times(1)).saveCountryData(countryData);
    }

    @Test
    void shouldUseCachedCountryDataWhenApiFails() throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData cachedCountryData = createTestCountryData(
                createTestRecord("record-1", 1)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(cachedCountryData);

        when(countryService.fetchCountryData())
                .thenThrow(new IOException("API unavailable"));

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());
        assertEquals("level-uuid", result.getFirst().getLevel().getId());
        assertEquals(1, result.getFirst().getRecords().size());

        verify(countryService, never()).saveCountryData(any(CountryData.class));
        verify(countryService, never()).getRecordsForLevel(anyString());
    }

    @Test
    void shouldReuseCachedRecordsWhenCompletionCountIsUnchanged()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData previousCountryData = createTestCountryData(
                createTestRecord("record-1", 3)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(previousCountryData);

        CountryRecord currentFirstVictor = createTestRecord("record-1", 3);
        CountryRecord secondVictor = createTestRecord("record-2", 3);

        CountryData currentCountryData = createTestCountryData(currentFirstVictor);

        when(countryService.fetchCountryData())
                .thenReturn(currentCountryData);

        LevelRecordsCache cachedRecords = createTestRecordsCache(
                3,
                List.of(currentFirstVictor, secondVictor)
        );

        when(countryService.getCachedRecordsForLevel("level-uuid"))
                .thenReturn(cachedRecords);

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().getRecords().size());

        verify(countryService, times(1))
                .getCachedRecordsForLevel("level-uuid");

        verify(countryService, never()).getRecordsForLevel(anyString());

        verify(countryService, never()).saveRecordsForLevel(
                anyString(), anyInt(), anyList()
        );

        verify(countryService, times(1)).saveCountryData(currentCountryData);
    }

    @Test
    void shouldFetchAndUpdateIndividualRecordsWhenCompletionCountChanges()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData previousCountryData = createTestCountryData(
                createTestRecord("record-1", 3)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(previousCountryData);

        CountryRecord currentVictor = createTestRecord("record-1", 4);

        CountryData currentCountryData = createTestCountryData(currentVictor);

        when(countryService.fetchCountryData())
                .thenReturn(currentCountryData);

        LevelRecordsCache cachedRecords = createTestRecordsCache(
                3,
                List.of(
                        createTestRecord("record-1", 3),
                        createTestRecord("record-2", 3)
                )
        );

        when(countryService.getCachedRecordsForLevel("level-uuid"))
                .thenReturn(cachedRecords);

        List<CountryRecord> fetchedRecords = List.of(
                currentVictor,
                createTestRecord("record-2", 4),
                createTestRecord("record-3", 4),
                createTestRecord("record-4", 4)
        );

        when(countryService.getRecordsForLevel("level-uuid"))
                .thenReturn(fetchedRecords);

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());
        assertEquals(4, result.getFirst().getRecords().size());

        verify(countryService).getCachedRecordsForLevel("level-uuid");
        verify(countryService).getRecordsForLevel("level-uuid");

        verify(countryService).saveRecordsForLevel(
                "level-uuid", 4, fetchedRecords
        );

        verify(countryService).saveCountryData(currentCountryData);
    }

    @Test
    void shouldFetchIndividualRecordsWhenCacheIsCorrupted()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData previousCountryData = createTestCountryData(
                createTestRecord("record-1", 3)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(previousCountryData);

        CountryRecord currentVictor = createTestRecord("record-1", 4);

        CountryData currentCountryData = createTestCountryData(currentVictor);

        when(countryService.fetchCountryData())
                .thenReturn(currentCountryData);

        when(countryService.getCachedRecordsForLevel("level-uuid"))
                .thenThrow(new IOException("Caché individual corrupta"));

        List<CountryRecord> fetchedRecords = List.of(
                currentVictor,
                createTestRecord("record-2", 4)
        );

        when(countryService.getRecordsForLevel("level-uuid"))
                .thenReturn(fetchedRecords);

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().getRecords().size());

        verify(countryService).getRecordsForLevel("level-uuid");

        verify(countryService).saveRecordsForLevel(
                "level-uuid", 4, fetchedRecords
        );

        verify(countryService).saveCountryData(currentCountryData);
    }

    @Test
    void shouldNotSaveCountryDataWhenIndividualRecordsFetchFails()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData previousCountryData = createTestCountryData(
                createTestRecord("record-1", 3)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(previousCountryData);

        CountryRecord currentVictor = createTestRecord("record-1", 4);

        CountryData currentCountryData = createTestCountryData(currentVictor);

        when(countryService.fetchCountryData())
                .thenReturn(currentCountryData);

        LevelRecordsCache cachedRecords = createTestRecordsCache(
                3,
                List.of(
                        createTestRecord("record-1", 3),
                        createTestRecord("record-2", 3)
                )
        );

        when(countryService.getCachedRecordsForLevel("level-uuid"))
                .thenReturn(cachedRecords);

        when(countryService.getRecordsForLevel("level-uuid"))
                .thenThrow(new IOException("Error consultando récords individuales"));

        TrackerService tracker = new TrackerService(levelService, countryService);

        assertThrows(IOException.class, tracker::getTrackedLevels);

        verify(countryService).getRecordsForLevel("level-uuid");

        verify(countryService, never()).saveCountryData(any());

        verify(countryService, never()).saveRecordsForLevel(
                anyString(), anyInt(), anyList()
        );
    }

    @Test
    void shouldNotFetchIndividualRecordsWhenCountryApiFails()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        CountryData previousCountryData = createTestCountryData(
                createTestRecord("record-1", 3)
        );

        when(countryService.getCatchedCountryData())
                .thenReturn(previousCountryData);

        when(countryService.fetchCountryData())
                .thenThrow(new IOException("API unavailable"));

        LevelRecordsCache cachedRecords = createTestRecordsCache(
                2,
                List.of(
                        createTestRecord("record-1", 2),
                        createTestRecord("record-2", 2)
                )
        );

        when(countryService.getCachedRecordsForLevel("level-uuid"))
                .thenReturn(cachedRecords);

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());

        verify(countryService, never()).getRecordsForLevel(anyString());

        verify(countryService, never()).saveRecordsForLevel(
                anyString(), anyInt(), anyList()
        );

        verify(countryService, never()).saveCountryData(any());
    }

    @Test
    void shouldContinueWhenPreviousCountryCacheIsCorrupted()
            throws Exception {

        AredlLevelService levelService = mock(AredlLevelService.class);
        CountryService countryService = mock(CountryService.class);

        Level level = createTestLevel();

        when(levelService.fetchLevels()).thenReturn(List.of(level));

        when(countryService.getCatchedCountryData())
                .thenThrow(new IOException("Caché general corrupta"));

        CountryData currentCountryData = createTestCountryData(
                createTestRecord("record-1", 1)
        );

        when(countryService.fetchCountryData())
                .thenReturn(currentCountryData);

        TrackerService tracker = new TrackerService(levelService, countryService);

        List<TrackedLevel> result = tracker.getTrackedLevels();

        assertEquals(1, result.size());

        verify(countryService).fetchCountryData();
        verify(countryService).saveCountryData(currentCountryData);

        verify(countryService, never()).getRecordsForLevel(anyString());
    }

    // --------------------------------------------------
    // MÉTODOS AUXILIARES
    // --------------------------------------------------

    private Level createTestLevel() {
        Level level = new Level();
        level.setId("level-uuid");
        level.setName("Test Level");
        level.setPosition(1);
        return level;
    }

    private CountryRecord createTestRecord(String id, int completionCount) {
        CountryRecord record = new CountryRecord();
        record.setId(id);
        record.setLevel(createTestLevel());
        record.setCompletionCount(completionCount);
        return record;
    }

    private CountryData createTestCountryData(CountryRecord... records) {
        CountryData countryData = new CountryData();
        countryData.setRecords(List.of(records));
        return countryData;
    }

    private LevelRecordsCache createTestRecordsCache(
            int completionCount,
            List<CountryRecord> records
    ) {
        LevelRecordsCache cache = new LevelRecordsCache();
        cache.setCompletionCount(completionCount);
        cache.setRecords(records);
        return cache;
    }
}
