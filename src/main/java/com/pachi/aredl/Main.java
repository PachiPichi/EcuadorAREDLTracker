package com.pachi.aredl;

import com.pachi.aredl.api.AredlApiClient;
import com.pachi.aredl.model.Level;
import com.pachi.aredl.service.AredlLevelService;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        AredlLevelService service = new AredlLevelService();

        try {
            List<Level> levels = service.getAllLevels();

            System.out.println("Niveles recibidos: " + levels.size());
            System.out.println();

            levels.stream()
                    .limit(10)
                    .forEach(level ->
                            System.out.printf(
                                    "Posición: %s | Nombre: %s | Tier NLW: %s%n",
                                    level.getPosition(),
                                    level.getName(),
                                    level.getNlwTier()
                            )
                    );
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
