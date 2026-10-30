
package com.pachi.aredl.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class AredlApiClient {

    private static final String BASE_URL =
            "https://api.aredl.net/v2";
    private static final String COUNTRY_CODE = "218";

    private final HttpClient httpClient;

    public AredlApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private String getJson(String endpoint, boolean useCacheFirst)
            throws IOException, InterruptedException {

        Path cacheFile = Path.of(
                "data",
                "cache",
                "api",
                endpoint.replaceAll("[^a-zA-Z0-9._-]", "_") + ".json"
        );

        if (useCacheFirst && Files.exists(cacheFile)) {
            return Files.readString(cacheFile);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;

        try {

         response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        } catch (IOException e) {
            if (Files.exists(cacheFile)) {
                return Files.readString(cacheFile);
            }

            throw e;
        }

        if (response.statusCode() != 200) {
            if (Files.exists(cacheFile)) {
                return Files.readString(cacheFile);
            }

            throw new IOException(
                    "Error al consultar AREDL. Endpoint: "
                    + endpoint
                    + ", Código HTTP: "
                    + response.statusCode()
            );
        }

        String json = response.body();

        Files.createDirectories(cacheFile.getParent());
        Files.writeString(cacheFile, json);

        return json;
    }

    public String getLevelsJson()
            throws IOException, InterruptedException {

        return getJson("/api/aredl/levels", false);
    }

    public String getCountryRecordsJson()
            throws IOException, InterruptedException {

        return getJson(
                "/api/aredl/country/" + COUNTRY_CODE, false
        );
    }

    public String getLevelRecordsJson(String levelId)
            throws IOException, InterruptedException {

        return getJson(
                "/api/aredl/country/" + COUNTRY_CODE
                        + "/levels/" + levelId + "/records", true
        );
    }
}