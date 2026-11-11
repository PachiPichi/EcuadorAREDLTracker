package com.pachi.aredl.service;

import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.LevelRecordsCache;
import com.pachi.aredl.model.level.TrackedLevel;

import java.io.IOException;
import java.util.*;

public class TrackerService {

    private final AredlLevelService levelService;
    private final CountryService countryService;

    public TrackerService(){
        this(
                new AredlLevelService(),
                new CountryService()
        );
    }

    public TrackerService(
            AredlLevelService levelService,
            CountryService countryService
    ){
        this.levelService = levelService;
        this.countryService = countryService;
    }

    public TrackerResult getTrackerResult()
            throws IOException, InterruptedException{

        List<Level> levels;
        boolean freshLevels = true;

        try {
            levels = levelService.fetchLevels();

            if (levels == null) {
                throw new IOException(
                        "La API no devolvió una lista válida de niveles."
                );
            }
        } catch (IOException e) {
            try {
                levels = levelService.getCachedLevels();
            } catch (IOException cacheError) {
                levels = null;
            }

            if (levels == null || levels.isEmpty()) {
                throw e;
            }

            System.out.println("No se pudieron descargar los niveles actuales. "
            + "Se utilizarán los niveles de caché.");

            freshLevels = false;
        }

        if (freshLevels) {
            levelService.saveLevels(levels);
        }

        CountryData previousCountryData = null;

        try {
            previousCountryData = countryService.getCatchedCountryData();
        } catch (IOException e) {
            System.out.println(
                    "La caché del país no es válida, "
                    + "se intentarán descargar datos actuales."
            );
        }

        CountryData currentCountryData;
        boolean freshCountryData = true;

        try {
            currentCountryData = countryService.fetchCountryData();
        } catch (IOException e) {
            if (previousCountryData == null) {
                throw e;
            }

            System.out.println("No se pudo consultar AREDL utilizando caché.");

            currentCountryData = previousCountryData;
            freshCountryData = false;
        }

        if (currentCountryData.getRecords() == null){
            throw new IOException(
                    "La respuesta de AREDL no contiene la lista de records."
            );
        }

        Map<String, CountryRecord> currentFirstVictors =
                getFirstVictorsByLevel(currentCountryData);

        Map<String, CountryRecord> previousFirstVictors =
                getFirstVictorsByLevel(previousCountryData);

        List<TrackedLevel> trackedLevels = new ArrayList<>();

        for (Level level : levels) {
            CountryRecord firstVictor =
                    currentFirstVictors.get(level.getId());

            List<CountryRecord> records = new ArrayList<>();

            if(firstVictor != null){
                records.add(firstVictor);

                Integer currentCount =
                        firstVictor.getCompletionCount();

                if (currentCount != null && currentCount > 1) {

                    CountryRecord previousVictor =
                            previousFirstVictors.get(level.getId());

                    Integer previousCount = previousVictor != null
                            ? previousVictor.getCompletionCount()
                            : null;

                    LevelRecordsCache cachedRecords = null;

                    try {
                        cachedRecords =
                                countryService.getCachedRecordsForLevel(
                                        level.getId()
                                );
                    } catch (IOException e) {
                        System.out.println(
                                "La caché del nivel " +  level.getName()
                                + " no tiene un formato válido, se descargará de nuevo."
                        );
                    }

                    boolean cacheMatchesCurrentCount =
                            cachedRecords != null
                            && Objects.equals(
                                    cachedRecords.getCompletionCount(),
                                    currentCount
                            )
                            && cachedRecords.getRecords() != null;

                    List<CountryRecord> additionalRecords;

                    if (cacheMatchesCurrentCount) {

                        additionalRecords = cachedRecords.getRecords();

                        if (Objects.equals(previousCount, currentCount)) {
                            System.out.println(
                                    "Sin cambios: " + level.getName()
                            );
                        } else {
                            System.out.println(
                                    "Nuevos records: " + level.getName()
                            );
                        }
                    } else if (freshCountryData) {
                        System.out.println(
                                "Nuevo nivel con mínimo 2 victors: " + level.getName()
                        );

                        additionalRecords =
                                countryService.getRecordsForLevel(
                                        level.getId()
                                );

                        if (additionalRecords == null) {
                            throw new IOException(
                                    "LA API devolvió una lista nula para "
                                    + level.getName()
                            );
                        }

                        countryService.saveRecordsForLevel(
                                level.getId(),
                                currentCount,
                                additionalRecords
                        );
                    } else {
                        additionalRecords = cachedRecords != null
                                && cachedRecords.getRecords() != null
                            ? cachedRecords.getRecords()
                            : List.of();

                        System.out.println(
                                "Sin conexión; se utilizan los últimos "
                                + "récords disponibles de " + level.getName()
                        );
                    }

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

        if (freshCountryData) {
            countryService.saveCountryData(currentCountryData);
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

        return new TrackerResult(
                currentCountryData.getRank(),
                trackedLevels
        );
    }

    public List<TrackedLevel> getTrackedLevels()
            throws IOException, InterruptedException {

        return getTrackerResult().getTrackedLevels();
    }

    private Map<String, CountryRecord> getFirstVictorsByLevel(
            CountryData countryData
    ) {
        Map<String, CountryRecord> firstVictors = new HashMap<>();

        if (countryData == null || countryData.getRecords() == null) {
            return firstVictors;
        }

        for (CountryRecord record : countryData.getRecords()) {
            if (record.getLevel() != null
                && record.getLevel().getId() != null) {
                firstVictors.putIfAbsent(
                        record.getLevel().getId(),
                        record
                );
            }
        }
        return firstVictors;
    }
}
