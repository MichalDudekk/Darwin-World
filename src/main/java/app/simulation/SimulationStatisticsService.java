package app.simulation;

import app.model.*;
import app.shared.Config;
import java.util.*;
import java.util.stream.Collectors;

public class SimulationStatisticsService {
    private final Config config;
    private LifespanStats lifespanStats = new LifespanStats(0, 0);

    public SimulationStatisticsService(Config config) {
        this.config = config;
    }

    public SimulationState createSimulationState(AbstractWorldMap map, int days) {
        List<Animal> animals = map.getAnimals();
        Map<Vector2d, Plant> plants = map.getPlants();

        Statistics stats = calculateStats(animals, plants.size(), map);
        Map<Vector2d, List<AnimalInfo>> snapshot = createSnapshot(map.getFields());

        return new SimulationState(snapshot, new HashMap<>(plants), stats);
    }

    public void updateLifespanStats(List<Animal> dead) {
        if(dead.isEmpty()) return;
        int totalDead = lifespanStats.totalAnimals() + dead.size();
        int totalAge = lifespanStats.totalLifespan() + dead.stream().mapToInt(Animal::getAge).sum();
        this.lifespanStats = new LifespanStats(totalDead, totalAge);
    }

    private Statistics calculateStats(List<Animal> animals, int plantsCount, AbstractWorldMap map) {
        if (animals.isEmpty())
            return new Statistics(0, plantsCount, config.mapWidth()*config.mapHeight() - plantsCount, 0, 0, 0, 0, List.of());

        int totalEnergy = 0;
        int totalChildren = 0;
        int maxEnergy = 0;

        for(Animal a : animals) {
            totalEnergy += a.getEnergy();
            totalChildren += a.getChildrenCount();
            maxEnergy = Math.max(maxEnergy, a.getEnergy());
        }

        Set<Vector2d> occupiedFields = new HashSet<>(map.getPlants().keySet());

        map.getFields().forEach((position, animalList) -> {
            if(!animalList.isEmpty()) {
                occupiedFields.add(position);
            }
        });

        int totalFields = config.mapWidth() * config.mapHeight();
        int freeFields = totalFields - occupiedFields.size();

        double avgLife = lifespanStats.totalAnimals() > 0 ? (double)lifespanStats.totalLifespan()/lifespanStats.totalAnimals() : 0;

        return new Statistics(
                animals.size(), plantsCount, freeFields,
                (double) totalEnergy / animals.size(), avgLife,
                (double) totalChildren / animals.size(),
                maxEnergy, findTopGenotypes(animals)
        );
    }

    private List<Map.Entry<String, Integer>> findTopGenotypes(List<Animal> animals) {
        return animals.stream()
                .collect(Collectors.groupingBy(a -> a.getGenotype().toString(), Collectors.summingInt(x->1)))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3).toList();
    }

    private Map<Vector2d, List<AnimalInfo>> createSnapshot(Map<Vector2d, List<Animal>> fields) {
        Map<Vector2d, List<AnimalInfo>> res = new HashMap<>();
        fields.forEach((pos, list) -> {
            if(!list.isEmpty()) {
                res.put(pos, list.stream().map(a -> new AnimalInfo(
                        a.getPosition(), a.getEnergy(), a.getGenotype().toString(), a.getId(),
                        a.getGenotype().getActiveGene(), a.getEatenPlants(), a.getChildrenCount(),
                        a.getDescendantsCount(), a.getAge()
                )).toList());
            }
        });
        return res;
    }
}