package com.pachi.aredl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.storage.CacheService;

import java.io.IOException;
import java.util.List;

public class AredlLevelService {

    private static final String LEVELS_CACHE = "levels.json";

    private final AredlApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    public AredlLevelService(){
        this.apiClient = new AredlApiClient();
        this.objectMapper = new ObjectMapper();
        this.cacheService = new CacheService();


    }

    public List<Level> getCachedLevels() throws IOException {
        if (!cacheService.exists(LEVELS_CACHE)) {
            return null;
        }

        String json = cacheService.readJson(LEVELS_CACHE);

        return objectMapper.readValue(
                json,
                new TypeReference<List<Level>>(){}
        );
    }


    public List<Level> fetchLevels()
            throws IOException, InterruptedException {

        String json = apiClient.getLevelsJson();

        List<Level> levels = objectMapper.readValue(
                json,
                new TypeReference<List<Level>>() {
                }
        );

        // Comprobar que la respuesta contiene niveles.
        if (levels == null || levels.isEmpty()) {
            throw new IOException(
                    "AREDl devolvió una lista de niveles vacía o nula."
            );
        }

        // Comprobar que todos los niveles tienen identificador.
        for (Level level : levels) {
            if (level == null
                    || level.getId() == null
                    || level.getId().isBlank()) {

                throw new IOException(
                        "AREDl devolvió un nivel sin identificador válido."
                );
            }
        }

        return levels;
    }


    public void saveLevels(List<Level> levels) throws IOException {
        String json = objectMapper.writeValueAsString(levels);

        cacheService.writeJson(LEVELS_CACHE, json);
    }
}
