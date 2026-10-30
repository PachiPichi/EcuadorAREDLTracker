package com.pachi.aredl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.storage.CacheService;

import java.io.IOException;
import java.util.List;

public class CountryService {

    private final AredlApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    public CountryService(){
        this.apiClient = new AredlApiClient();
        this.objectMapper = new ObjectMapper();
        this.cacheService = new CacheService();
    }

    public CountryData getCountryData()
        throws IOException, InterruptedException {

        String json;
        try {
        json = apiClient.getCountryRecordsJson();
        cacheService.writeJson("country.json", json);
        } catch (IOException e) {
            if (!cacheService.exists("country.json")) {
                throw e;
            }

            System.out.println(
                    "No se pudo consultar AREDL. "
                    + "Utilizando la caché anterior de country.json."
            );

            json = cacheService.readJson("country.json");
        }

        return objectMapper.readValue(json, CountryData.class);
    }

    public List<CountryRecord> getRecordsForLevel(String levelId)
        throws IOException, InterruptedException{

        String json = apiClient.getLevelRecordsJson(levelId);

        return objectMapper.readValue(
                json,
                new TypeReference<>() {}
        );
    }
}
