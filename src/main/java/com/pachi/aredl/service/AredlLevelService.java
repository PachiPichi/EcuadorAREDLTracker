package com.pachi.aredl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.Level;

import java.io.IOException;
import java.util.List;

public class AredlLevelService {

    private final AredlApiClient apiClient;
    private final ObjectMapper objectMapper;

    public AredlLevelService(){
        this.apiClient = new AredlApiClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Level> getAllLevels()
    throws IOException, InterruptedException {
        String json = apiClient.getLevelsJson();

        return objectMapper.readValue(
                json, new TypeReference<List<Level>>(){}
        );
    }
}
