package com.pachi.aredl;

import com.pachi.aredl.model.CountryData;
import com.pachi.aredl.service.CountryService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        CountryService service = new CountryService();

        try {
            CountryData data = service.getCountryData();

            System.out.println("Clasificación: " + data.getRank().getRank());
            System.out.println("Records recibidos: "
                    + data.getRecords().size());
            System.out.println();

            data.getRecords().stream()
                    .limit(10)
                    .forEach(record -> {
                        String levelName = record.getLevel() != null
                                ? record.getLevel().getName()
                                : "Nivel desconocido";

                        String playerName = record.getSubmittedBy() != null
                                ? record.getSubmittedBy().getGlobal_name()
                                : "Jugador desconocido";

                        System.out.printf(
                                "Nivel: %s | Jugador: %s | Completados: %s%n",
                                levelName,
                                playerName,
                                record.getCompletionCount()
                        );
                    });
        } catch (IOException e) {

            System.err.println(
                    "No se pudo obtener la respuesta de AREDL: "
                            + e.getMessage()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(
                    "La petición fue interrumpida."
            );
        }
    }
}
