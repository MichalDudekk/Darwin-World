package app.simulation;

import app.model.*;
import app.shared.Config;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation implements Runnable {
    private final UUID id = new UUID(5, 10);
    private final Config config;
    private final AbstractWorldMap map;
    private final List<SimulationState> history = new ArrayList<>();
//    private final StatisticsLogger statisticsLogger;
    private final List<MapChangeListener> listeners = new ArrayList<>();

    private volatile boolean isRunning = true;
    private volatile boolean isPaused;

    public Simulation(Config config) {
        this.config = config;
        this.map = new GlobeMap(config, generateAnimals());
    }

    private void notifyListeners() {
        for (MapChangeListener listener : listeners) {
            listener.onMapChanged(map, "Dzień zakończony");
        }
    }

    public void addListener(MapChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MapChangeListener listener) {
        listeners.remove(listener);
    }

    public AbstractWorldMap getMap() {
        return map;
    }

    public UUID getId() {
        return id;
    }

    private List<Animal> generateAnimals() {
        List<Animal> animals = new ArrayList<>();

        MoveStrategy moveStrategy = config.isFomo() ?
                new FomoMoveStrategy(config.fomoRadius(), config.fomoCrowdSize()) :
                new StandardMoveStrategy();

        for(int i = 0; i < config.initialAnimalsNum(); i++) {
            List<Integer> animalGenoms = new Random()
                    .ints(config.genomLength(), 0, 7 + 1)
                    .boxed()
                    .toList();

            Animal animal = new Animal(
                    config.animalStartEnergy(),
                    Vector2d.getRandom(0, config.mapWidth() - 1, 0, config.mapHeight() - 1),
                    MapDirection.getRandom(),
                    new Genotype(animalGenoms),
                    moveStrategy
            );

            animals.add(animal);
        }

        return animals;
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

            // Day cycle //
            map.removeDeadAnimals();
            map.moveAnimals();
            map.animalsConsume();
            map.animalsReproduce();
            map.growPlants();
            map.dayPassed();

            notifyListeners();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void resume() {
        isPaused = false;
        notifyAll();
    }

    public void end() {
        isRunning = false;
        resume();
    }

}
