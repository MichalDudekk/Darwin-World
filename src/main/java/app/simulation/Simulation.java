package app.simulation;

import app.model.*;
import app.shared.Config;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation implements Runnable {
    private final UUID id = UUID.randomUUID();
    private final Config config;
    private final AbstractWorldMap map;
    private final List<SimulationState> history = new ArrayList<>();
//    private final StatisticsLogger statisticsLogger;
    private final List<MapChangeListener> listeners = new ArrayList<>();
    private LifespanStats accumulatedLifespanStats = new LifespanStats(0, 0);

    private volatile boolean isRunning = true;
    private volatile boolean isPaused = true;

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

    public List<SimulationState> getHistory() {
        return history;
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
        while (isRunning) {
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

           simulateDay();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void simulateDay() {
        List<Animal> deadAnimals = map.removeDeadAnimals();
        updateLifespanStats(deadAnimals);

        map.moveAnimals();
        map.animalsConsume();
        map.animalsReproduce();
        map.growPlants();
        map.dayPassed();

        saveSimulationState();

        notifyListeners();
    }

    public void step() {
        synchronized (this) {
            if (isPaused) {
                simulateDay();
            }
        }
    }

    private void updateLifespanStats(List<Animal> deadAnimalsToday) {
        if (deadAnimalsToday.isEmpty()) return;

        int currentTotalCount = accumulatedLifespanStats.totalAnimals();
        int currentTotalLifespan = accumulatedLifespanStats.totalLifespan();

        int newDeadCount = deadAnimalsToday.size();
        int newLifespanSum = 0;

        for (Animal animal : deadAnimalsToday) {
            newLifespanSum += animal.getAge();
        }

        this.accumulatedLifespanStats = new LifespanStats(
                currentTotalCount + newDeadCount,
                currentTotalLifespan + newLifespanSum
        );
    }

    private void saveSimulationState() {
        List<Animal> allAnimals = map.getAnimals();
        Map<Vector2d, List<Animal>> currentAnimalsMap = map.getFields();
        Map<Vector2d, Plant> currentPlantsMap = map.getPlants();

        int animalsCount = allAnimals.size();
        int plantsCount = currentPlantsMap.size();

        double totalEnergy = 0;
        double totalChildren = 0;
        int maxEnergy = 0;

        for (Animal animal : allAnimals) {
            totalEnergy += animal.getEnergy();
            totalChildren += animal.getChildrenCount();

            if (animal.getEnergy() > maxEnergy) {
                maxEnergy = animal.getEnergy();
            }
        }

        double avgEnergy = animalsCount > 0 ? totalEnergy / animalsCount : 0;
        double avgChildren = animalsCount > 0 ? totalChildren / animalsCount : 0;

        int mapWidth = config.mapWidth();
        int mapHeight = config.mapHeight();
        int totalFields = mapWidth * mapHeight;

        Set<Vector2d> occupiedFields = new HashSet<>(currentPlantsMap.keySet());

        for (Map.Entry<Vector2d, List<Animal>> entry : currentAnimalsMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                occupiedFields.add(entry.getKey());
            }
        }

        int freeFields = totalFields - occupiedFields.size();

        double avgLifespan = 0;
        if (this.accumulatedLifespanStats.totalAnimals() > 0) {
            avgLifespan = (double) this.accumulatedLifespanStats.totalLifespan()
                    / this.accumulatedLifespanStats.totalAnimals();
        }

        Map<String, Integer> genotypeCounts = map.getAnimals().stream()
                .collect(Collectors.groupingBy(
                        animal -> animal.getGenotype().toString(),
                        Collectors.summingInt(x -> 1)
                ));

        List<Map.Entry<String, Integer>> top3Genotypes = genotypeCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sortowanie malejące
                .limit(3)
                .toList();

        Statistics statistics = new Statistics(
                animalsCount,
                plantsCount,
                freeFields,
                avgEnergy,
                avgLifespan,
                avgChildren,
                maxEnergy,
                top3Genotypes
        );

        Map<Vector2d, List<AnimalInfo>> animalSnapshot = new HashMap<>();

        for (Map.Entry<Vector2d, List<Animal>> entry : map.getFields().entrySet()) {
            Vector2d pos = entry.getKey();
            List<Animal> liveAnimals = entry.getValue();

            List<AnimalInfo> infoList = new ArrayList<>();

            for (Animal animal : liveAnimals) {
                infoList.add(new AnimalInfo(
                        animal.getPosition(),
                        animal.getEnergy(),
                        animal.getGenotype().toString(),
                        animal.getId(),
                        animal.getGenotype().getActiveGene(),
                        animal.getEatenPlants(),
                        animal.getChildrenCount(),
                        animal.getDescendantsCount(),
                        animal.getAge(),
                        animal.getDeathDate()
                ));
            }

            if (!infoList.isEmpty()) {
                animalSnapshot.put(pos, infoList);
            }
        }

        Map<Vector2d, Plant> plantPositionsSnapshot = new HashMap<>(currentPlantsMap);

        SimulationState newState = new SimulationState(
                animalSnapshot,
                plantPositionsSnapshot,
                statistics
        );

        this.history.add(newState);
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
