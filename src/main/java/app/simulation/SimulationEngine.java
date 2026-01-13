package app.simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulationEngine {
    private final ExecutorService executorService;

    public SimulationEngine() {
        this.executorService = Executors.newCachedThreadPool();
    }

    public void runAsync(Simulation simulation) {
        executorService.submit(simulation);
    }

    public void awaitSimulationsEnd() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}
