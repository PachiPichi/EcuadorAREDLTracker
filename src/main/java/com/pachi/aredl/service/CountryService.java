package com.pachi.aredl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.country.CountryRecord;

import java.io.IOException;
import java.util.List;

public class CountryService {

    private final AredlApiClient apiClient;
    private final ObjectMapper objectMapper;

    public CountryService(){
        this.apiClient = new AredlApiClient();
        this.objectMapper = new ObjectMapper();
    }

    public CountryData getCountryData()
        throws IOException, InterruptedException{
        String json = apiClient.getCountryRecordsJson();
        return objectMapper.readValue(json, CountryData.class);
    }

    public List<CountryRecord> getRecordsForLevel(String levelId)
        throws IOException, InterruptedException{
        String json = apiClient.getLevelRecordsJson(levelId);

        return objectMapper.readValue(
                json,
                new TypeReference<List<CountryRecord>>() {}
        );
    }
}
