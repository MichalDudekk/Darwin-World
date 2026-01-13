package app.model;

import app.shared.Config;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractWorldMap implements WorldMap {
    protected Config config;

    protected int jungleBottomY;
    protected int jungleTopY;

    protected Map<Vector2d, List<Animal>> fields;
    protected Map<Vector2d, Plant> plants = new HashMap<>();
    protected List<Animal> animals = new ArrayList<>();

    protected int daysElapsed = 0;

    @Override
    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }

    @Override
    public Map<Vector2d, List<Animal>> getFields() {
        return fields;
    }

    @Override
    public List<Animal> getAnimals() {
        return animals;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    @Override
    public void addAnimal(Animal animal) {
        animals.add(animal);
        fields.get(animal.getPosition()).add(animal);
    }

    @Override
    public List<Animal> removeDeadAnimals() {
        List<Animal> deadAnimals = new ArrayList<>();

        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            if (animal.getEnergy() <= 0) {
                iterator.remove();
                fields.get(animal.getPosition()).remove(animal); // removing from fields too
                deadAnimals.add(animal);
            }
        }

        return deadAnimals;
    }

    @Override
    public void animalsConsume() {
        Vector2d[] vectors = plants.keySet().toArray(new Vector2d[0]);

        for (Vector2d v : vectors) {
            List<Animal> residents = fields.get(v);
            if (residents.isEmpty()) continue;

            residents.sort(Comparator.reverseOrder()); // sortowanie malejące
            residents.getFirst().eat(plants.get(v));
            plants.remove(v);
        }
    }

    @Override
    public void animalsReproduce() {
        Vector2d[] vectors = fields.keySet().toArray(new Vector2d[0]);

        for (Vector2d v : vectors) {
            List<Animal> residents = fields.get(v);
            if (residents.size() <= 1)
                continue;
            residents.sort(Comparator.reverseOrder()); // sortowanie malejące

            if (residents.get(1).getEnergy() < config.energyNeededToReproduce())
                continue;

            Animal child = residents.get(0).reproduce(residents.get(1), config.energyUsedToReproduce());
            child.getGenotype().mutate(config.minMutations(), config.maxMutations());
            this.addAnimal(child);
        }
    }

    @Override
    public void addPlant(Vector2d position, Plant plant) {
        if (plants.containsKey(position)) {
            throw new IllegalArgumentException("Position %s already has a plant".formatted(position));
        }
        plants.put(position,plant);
    }

    private int plantSeeds(List<Vector2d> spots, int count) {
        int actualCount = Math.min(count, spots.size());

        for (int i = 0; i < actualCount; i++) {
            int energy = ThreadLocalRandom.current().nextInt(
                    config.plantMinEnergy(), config.plantMaxEnergy() + 1
            );
            addPlant(spots.get(i), new Plant(energy));
        }

        return actualCount;
    }

    @Override
    public void growPlants() {
        growPlants(config.dailyPlantGrowth());
    }

    @Override
    public void growPlants(int toBeGrown) {
        List<Vector2d> jungleFree = new ArrayList<>();
        List<Vector2d> stepFree = new ArrayList<>();

        for (int x = 0; x < config.mapWidth(); x++) {
            for (int y = 0; y < config.mapHeight(); y++) {
                Vector2d v = new Vector2d(x, y);
                if (plants.containsKey(v)) continue;

                if (y >= jungleBottomY && y <= jungleTopY) {
                    jungleFree.add(v);
                } else {
                    stepFree.add(v);
                }
            }
        }

        Collections.shuffle(jungleFree);
        Collections.shuffle(stepFree);

        int jungleTarget = (int) (toBeGrown * 0.8); // 80%
        int plantedInJungle = plantSeeds(jungleFree, jungleTarget);

        int stepTarget = toBeGrown - plantedInJungle;
        plantSeeds(stepFree, stepTarget);
    }

    @Override
    public void dayPassed() {
        daysElapsed++;
        for (Animal animal : animals) {
            animal.setEnergy(animal.getEnergy() - config.animalEnergyLoss());
            animal.setAge(animal.getAge() + 1);
        }
    }

    @Override
    public List<Animal> getAnimalsInRadius(Vector2d center, int radius) {
        List<Animal> res = new ArrayList<>();

        for (Animal animal : animals) {
            double distance = center.calculateDistance(animal.getPosition());

            if (distance <= radius) {
                res.add(animal);
            }
        }
        return res;
    }
}
