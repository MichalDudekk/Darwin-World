package app.simulation;

import app.model.*;
import app.shared.Config;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation implements Runnable {
    private static final String SIMULATION_LOGGER_FILE_PREFIX = "./statistics/statystyki";

    private final UUID id = UUID.randomUUID();
    private final Config config;
    private final AbstractWorldMap map;
    private final List<SimulationState> history = new ArrayList<>();
    private final SimulationLogger logger;
    private final List<MapChangeListener> listeners = new ArrayList<>();
    private LifespanStats accumulatedLifespanStats = new LifespanStats(0, 0);

    private volatile boolean isRunning = true;
    private volatile boolean isPaused = true;
    private boolean stepRequest = false;

    // CONSTRUCTOR //

    public Simulation(Config config) {
        this.config = config;
        this.map = new GlobeMap(config, generateAnimals());
        this.logger = new SimulationLogger(SIMULATION_LOGGER_FILE_PREFIX);
    }

    // PUBLIC API //

    @Override
    public void run() {
        while (isRunning) {
            synchronized (this) {
                while (isPaused && !stepRequest) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (stepRequest) {
                    stepRequest = false;
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

    public void step() {
        synchronized (this) {
            if (isPaused) {
                stepRequest = true;
                notifyAll();
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
        logger.close();
    }

    // GETTERS AND LISTENERS //

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

    // CORE LOGIC //

    private void simulateDay() {
        List<Animal> deadAnimals = map.removeDeadAnimals();
        updateLifespanStats(deadAnimals);

        map.moveAnimals();
        map.animalsConsume();
        map.animalsReproduce();
        map.growPlants();
        map.dayPassed();

        SimulationState simulationState = saveSimulationState();
        logger.logDay(map.getDaysElapsed(), simulationState.statistics());

        notifyListeners();
    }

    // DATA PROCESSING //

    private SimulationState saveSimulationState() {
        List<Animal> allAnimals = map.getAnimals();
        Map<Vector2d, Plant> currentPlants = map.getPlants();

        Statistics stats = calculateStatistics(allAnimals, currentPlants.size());

        Map<Vector2d, List<AnimalInfo>> animalSnapshot = createAnimalSnapshot(map.getFields());

        SimulationState newState = new SimulationState(
                animalSnapshot,
                new HashMap<>(currentPlants),
                stats
        );

        this.history.add(newState);
        return newState;
    }

    private Statistics calculateStatistics(List<Animal> animals, int plantsCount) {
        if (animals.isEmpty()) {
            return new Statistics(0, plantsCount, config.mapWidth() * config.mapHeight(), 0, 0, 0, 0, List.of());
        }

        double totalEnergy = 0;
        double totalChildren = 0;
        int maxEnergy = 0;

        for (Animal animal : animals) {
            totalEnergy += animal.getEnergy();
            totalChildren += animal.getChildrenCount();
            maxEnergy = Math.max(maxEnergy, animal.getEnergy());
        }

        double avgEnergy = totalEnergy / animals.size();
        double avgChildren = totalChildren / animals.size();

        int occupiedCount = countOccupiedFields();
        int freeFields = (config.mapWidth() * config.mapHeight()) - occupiedCount;

        List<Map.Entry<String, Integer>> topGenotypes = findTopGenotypes(animals);

        double avgLifespan = accumulatedLifespanStats.totalAnimals() > 0 ?
                (double) accumulatedLifespanStats.totalLifespan() / accumulatedLifespanStats.totalAnimals() : 0;

        return new Statistics(
                animals.size(),
                plantsCount,
                freeFields,
                avgEnergy,
                avgLifespan,
                avgChildren,
                maxEnergy,
                topGenotypes
        );
    }

    private Map<Vector2d, List<AnimalInfo>> createAnimalSnapshot(Map<Vector2d, List<Animal>> worldMapFields) {
        Map<Vector2d, List<AnimalInfo>> snapshot = new HashMap<>();

        for (var entry : worldMapFields.entrySet()) {
            List<Animal> liveAnimals = entry.getValue();
            if (liveAnimals.isEmpty()) continue;

            List<AnimalInfo> infoList = liveAnimals.stream()
                    .map(animal -> new AnimalInfo(
                            animal.getPosition(),
                            animal.getEnergy(),
                            animal.getGenotype().toString(),
                            animal.getId(),
                            animal.getGenotype().getActiveGene(),
                            animal.getEatenPlants(),
                            animal.getChildrenCount(),
                            animal.getDescendantsCount(),
                            animal.getAge()
                    ))
                    .toList();

            snapshot.put(entry.getKey(), infoList);
        }
        return snapshot;
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

    // LOW LEVEL HELPERS //

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

    private int countOccupiedFields() {
        Set<Vector2d> occupied = new HashSet<>(map.getPlants().keySet());

        for (var entry : map.getFields().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                occupied.add(entry.getKey());
            }
        }
        return occupied.size();
    }

    private List<Map.Entry<String, Integer>> findTopGenotypes(List<Animal> animals) {
        return animals.stream()
                .collect(Collectors.groupingBy(
                        animal -> animal.getGenotype().toString(),
                        Collectors.summingInt(x -> 1)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .toList();
    }

    private void notifyListeners() {
        for (MapChangeListener listener : listeners) {
            listener.onMapChanged(map, "Dzień zakończony");
        }
    }
}
