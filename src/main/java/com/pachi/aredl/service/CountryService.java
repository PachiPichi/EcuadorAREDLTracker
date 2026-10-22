package com.pachi.aredl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.country.CountryData;

import java.io.IOException;

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
}
