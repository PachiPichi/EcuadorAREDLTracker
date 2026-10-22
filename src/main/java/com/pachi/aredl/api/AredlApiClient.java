
package com.pachi.aredl.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private String getJson(String endpoint)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error al consultar AREDL. Endpoint: "
                            + endpoint
                            + ". Código HTTP: "
                            + response.statusCode()
            );
        }

        return response.body();
    }

    public String getLevelsJson()
            throws IOException, InterruptedException {

        return getJson("/api/aredl/levels");
    }

    public String getCountryRecordsJson()
            throws IOException, InterruptedException {

        return getJson(
                "/api/aredl/country/" + COUNTRY_CODE
        );
    }

    public String getLevelRecordsJson(String levelId)
            throws IOException, InterruptedException {

        return getJson(
                "/api/aredl/country/" + COUNTRY_CODE
                        + "/levels/" + levelId + "/records"
        );
    }
}