package app.simulation;

import app.model.AbstractWorldMap;
import app.model.Animal;
import app.model.GlobeMap;
import app.model.MapChangeListener;
import app.shared.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Simulation implements Runnable {
    private static final String LOGGER_PATH = "./statistics/statystyki";

    private final UUID id = UUID.randomUUID();
    private final AbstractWorldMap map;
    private final SimulationLogger logger;
    private final SimulationStatisticsService statsService;
    private final List<SimulationState> history = new ArrayList<>();
    private final List<MapChangeListener> listeners = new ArrayList<>();

    private volatile boolean isRunning = true;
    private volatile boolean isPaused = true;
    private boolean stepRequest = false;

    public Simulation(Config config) {
        this.statsService = new SimulationStatisticsService(config);
        this.map = new GlobeMap(config, PopulationGenerator.generate(config));
        this.logger = new SimulationLogger(LOGGER_PATH);
    }

    @Override
    public void run() {
        while (isRunning) {
            synchronized (this) {
                while (isPaused && !stepRequest) {
                    try { wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                stepRequest = false;
            }

            simulateDay();

            try {
                int moveDelay = 100;
                Thread.sleep(moveDelay); }
            catch (InterruptedException e) { break; }
        }
    }

    private void simulateDay() {
        List<Animal> dead = map.removeDeadAnimals();
        map.moveAnimals();
        map.animalsConsume();
        map.animalsReproduce();
        map.growPlants();
        map.dayPassed();

        statsService.updateLifespanStats(dead);
        SimulationState state = statsService.createSimulationState(map, map.getDaysElapsed());

        saveHistory(state);
        logger.logDay(map.getDaysElapsed(), state.statistics());
        notifyListeners();
    }

    private void saveHistory(SimulationState state) {
        synchronized(this) {
            history.add(state);
        }
    }

    public void step() {
        synchronized(this) {
            if(isPaused) {
                stepRequest = true; notifyAll();
            }
        }
    }

    public synchronized void pause() { isPaused = true; }
    public synchronized void resume() { isPaused = false; notifyAll(); }
    public void end() { isRunning = false; resume(); logger.close(); }
    public void addListener(MapChangeListener l) { listeners.add(l); }
    public List<SimulationState> getHistory() { return history; }
    public UUID getId() { return id; }

    private void notifyListeners() { listeners.forEach(l -> l.onMapChanged(map, "Day passed")); }
}