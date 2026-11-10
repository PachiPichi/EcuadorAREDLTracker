package com.pachi.aredl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.LevelRecordsCache;
import com.pachi.aredl.storage.CacheService;

import java.io.IOException;
import java.util.List;

public class CountryService {

    private static final String COUNTRY_CACHE = "country.json";

    private final AredlApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    public CountryService(){
        this.apiClient = new AredlApiClient();
        this.objectMapper = new ObjectMapper();
        this.cacheService = new CacheService();
    }

    //Recupera caché general
    public CountryData getCatchedCountryData()
        throws IOException{
        if (!cacheService.exists(COUNTRY_CACHE)) {
            return null;
        }

        String json = cacheService.readJson(COUNTRY_CACHE);

        return objectMapper.readValue(json, CountryData.class);
    }

    //Descarga datos generales sin guardar
    public CountryData fetchCountryData()
        throws IOException, InterruptedException{

        String json = apiClient.getCountryRecordsJson();

        CountryData countryData =
                objectMapper.readValue(json, CountryData.class);

        if (countryData == null || countryData.getRecords() == null) {
            throw new IOException(
                    "AREDL devolvió datos del país incompletos."
            );
        }
        return countryData;
    }

    //Guarda fetch al caché
    public void saveCountryData(CountryData countryData)
        throws IOException{

        if (countryData == null || countryData.getRecords() == null) {
            throw new IOException(
                    "No se pueden guardar datos inválidos."
            );
        }

        String json = objectMapper.writeValueAsString(countryData);

        cacheService.writeJson(COUNTRY_CACHE, json);
    }

    //Descarga datos individuales de nivel por UUID
    public List<CountryRecord> getRecordsForLevel(String levelId)
        throws IOException, InterruptedException{

        String json = apiClient.getLevelRecordsJson(levelId);

        List<CountryRecord> records = objectMapper.readValue(
                json,
                new TypeReference<>() {}
        );

        if (records == null || records.isEmpty()) {
            throw new IOException(
                    "AREDL devolvió una lista de records vacía para "
                    + levelId
            );
        }

        for (CountryRecord record : records) {
            if (record == null
            || record.getId() == null
            || record.getId().isBlank()){
                throw new IOException(
                        "AREDL devolvió un record sin un identificador válido en "
                        + levelId
                );
            }
        }
        return records;
    }

    //Lee caché individual de un nivel
    public LevelRecordsCache getCachedRecordsForLevel(String levelId)
        throws IOException{
        String fileName = "level-records/" + levelId + ".json";

        if (!cacheService.exists(fileName)) {
            return null;
        }

        String json = cacheService.readJson(fileName);

        return objectMapper.readValue(json, LevelRecordsCache.class);
    }

    //Guarda records y completion_count
    public void saveRecordsForLevel(
            String levelId,
            Integer completionCount,
            List<CountryRecord> records
    ) throws IOException{

        if (records == null || records.isEmpty()
        || completionCount == null || completionCount <= 1
        || levelId == null || levelId.isBlank()) {
            throw new IOException(
                    "No se pueden guardar récords del nivel: datos incompletos."
            );
        }

        for (CountryRecord record : records) {
            if (record == null
            || record.getId() == null
            || record.getId().isBlank()) {
                throw new IOException(
                        "No se pueden guardar récords del nivel: record sin identificador."
                );
            }
        }

        LevelRecordsCache cache = new LevelRecordsCache();
        cache.setCompletionCount(completionCount);
        cache.setRecords(records);

        String json = objectMapper.writeValueAsString(cache);

        String fileName = "level-records/" + levelId + ".json";

        cacheService.writeJson(fileName, json);
    }
}
