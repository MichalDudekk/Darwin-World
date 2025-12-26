package app.simulation;

import app.presenter.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Simulation implements Runnable {
    private final Params config;
    private final WorldMap map;
    private final UUID id = new UUID(10, 10);
    private final List<SimulationState> history = new ArrayList<>();
    private final StatisticsLogger statisticsLogger;

    private boolean isPaused;

    public Simulation(Params config) {
        this.config = config;

        this.map = new GlobeMap(config.mapWidth(), config.mapHeight());

        // Generowanie zwierząt
//        generateInitialAnimals(config.initialAnimalsNum());
    }

    @Override
    public void run() {
        while (!isPaused) {
            synchronized (this) {
                while (isPaused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            // Metody mapy removeDeadAnimals, moveAnimals, ...

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                break;
            }
        }
    }


}
