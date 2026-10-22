package com.pachi.aredl;

import com.pachi.aredl.model.country.CountryData;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.service.CountryService;
import com.pachi.aredl.service.TrackerService;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TrackerService trackerService = new TrackerService();

        try {
            List<TrackedLevel> levels =
                    trackerService.getTrackedLevels();

            long completedCount = levels.stream()
                    .filter(TrackedLevel::isCompleted)
                    .count();

            long pendingCount = levels.size() - completedCount;

            System.out.println("=== Ecuador AREDL Tracker ===");
            System.out.println("Total de niveles: " + levels.size());
            System.out.println("Completados: " + completedCount);
            System.out.println("Pendientes: " + pendingCount);
            System.out.println();

            System.out.println("=== TOP 1500 a 1530 ===");

            levels.stream()
                    .skip(1500)
                    .limit(30)
                    .forEach(trackedLevel -> {
                        var level = trackedLevel.getLevel();

                        String dlecPosition =
                                trackedLevel.getDlecPosition() != null
                                        ? trackedLevel.getDlecPosition().toString()
                                        : "-";

                        System.out.printf(
                                "AREDL #%s | Ecuador #%s | %s | Tier: %s | %s%n",
                                level.getPosition(),
                                dlecPosition,
                                level.getName(),
                                level.getNlwTier(),
                                trackedLevel.isCompleted()
                                        ? "COMPLETADO"
                                        : "PENDIENTE"
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
