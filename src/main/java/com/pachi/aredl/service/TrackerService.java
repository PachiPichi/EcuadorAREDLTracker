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

        Map<String, CountryRecord> recordsByLevelId = new HashMap<>();

        for(CountryRecord record : countryData.getRecords()){
            if (record.getLevel() != null
                    && record.getLevel().getId() != null) {

                recordsByLevelId.putIfAbsent(
                                record.getLevel().getId(),
                                record
                );
            }
        }

        List<TrackedLevel> trackedLevels = new ArrayList<>();

        for (Level level : levels) {
            CountryRecord record = recordsByLevelId.get(level.getId());

            trackedLevels.add(new TrackedLevel(level, record, null));
        }

        trackedLevels.sort(
                Comparator.comparing(
                        trackedLevel -> trackedLevel.getLevel().getPosition(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        List<TrackedLevel> completedLevels = trackedLevels.stream()
                .filter(TrackedLevel::isCompleted)
                .toList();

        int position = 1;
        for (TrackedLevel trackedLevel : completedLevels) {
            trackedLevel.setDlecPosition(position++);
        }

        return trackedLevels;
    }
}
