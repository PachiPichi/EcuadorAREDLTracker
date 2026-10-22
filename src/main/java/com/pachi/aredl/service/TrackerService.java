package com.pachi.aredl.service;

import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.TrackedLevel;

import java.io.IOException;
import java.util.*;

public class TrackerService {

    private final AredlLevelService levelService;
    private final CountryService countryService;

    public TrackerService(){
        this.levelService = new AredlLevelService();
        this.countryService = new CountryService();
    }

    public List<TrackedLevel> getTrackedLevels()
            throws IOException, InterruptedException{
        List<Level> levels = levelService.getAllLevels();
        CountryData countryData = countryService.getCountryData();

        Map<String, CountryRecord> firstVictorsByLevelId = new HashMap<>();

        for(CountryRecord record : countryData.getRecords()){
            if (record.getLevel() != null
                    && record.getLevel().getId() != null) {

                firstVictorsByLevelId.putIfAbsent(
                                record.getLevel().getId(),
                                record
                );
            }
        }

        List<TrackedLevel> trackedLevels = new ArrayList<>();

        for (Level level : levels) {
            CountryRecord firstVictor = firstVictorsByLevelId.get(level.getId());

            List<CountryRecord> records = new ArrayList<>();

            if(firstVictor != null){
                records.add(firstVictor);

                Integer completionCount =
                        firstVictor.getCompletionCount();

                if (completionCount != null && completionCount > 1) {
                    List<CountryRecord> additionalRecords =
                            countryService.getRecordsForLevel(level.getId());

                    Map<String, CountryRecord> uniqueRecords =
                            new LinkedHashMap<>();

                    for (CountryRecord record : records) {
                        if (record.getId() != null) {
                            uniqueRecords.putIfAbsent(
                                    record.getId(),
                                    record
                            );
                        }
                    }

                    for (CountryRecord record : additionalRecords) {
                        if (record.getId() != null) {
                            uniqueRecords.putIfAbsent(
                                    record.getId(),
                                    record
                            );
                        }
                    }

                    records = new ArrayList<>(uniqueRecords.values());
                }
            }
            trackedLevels.add(
                    new TrackedLevel(level, records, null)
            );
        }

        trackedLevels.sort(
                Comparator.comparing(
                        trackedLevel ->
                                trackedLevel.getLevel().getPosition(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        int position = 1;

        for (TrackedLevel trackedLevel : trackedLevels) {
            if (trackedLevel.isCompleted()){
            trackedLevel.setDlecPosition(position++);
            }
        }

        return trackedLevels;
    }
}
