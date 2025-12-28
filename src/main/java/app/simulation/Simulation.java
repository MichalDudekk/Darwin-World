package app.simulation;

import app.model.*;
import app.shared.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Simulation implements Runnable {
    private final Params config;
    private final AbstractWorldMap map;
    private final UUID id = new UUID(10, 10);
    private final List<SimulationState> history = new ArrayList<>();
//    private final StatisticsLogger statisticsLogger;

    private boolean isPaused;

    public Simulation(Params config) {
        this.config = config;
        this.map = new GlobeMap(config, generateAnimals());
    }

    public AbstractWorldMap getMap() {
        return map;
    }

    private List<Animal> generateAnimals() {
        List<Animal> animals = new ArrayList<>();
        MoveStrategy moveStrategy = config.isFomo() ?
                new FomoMoveStrategy(config.fomoRadius(), config.fomoCrowdSize()) :
                new StandardMoveStrategy();

        for(int i = 0; i < config.initialAnimalsNum(); i++) {
            // TODO: Trzeba dodac initial animal energy
            // TODO: Trzeba dodac losową pozycje
            Animal animal = new Animal(
                    10,
                    new Vector2d(4, 4),
                    MapDirection.getRandom(),
                    new Genotype(List.of(0,0,0)),
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

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }


}
