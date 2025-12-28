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
    protected List<MapChangeListener> listeners = new ArrayList<>();

    public void mapChanged(String message) {
        for (MapChangeListener listener : listeners) {
            listener.onMapChanged(this,message);
        }
    }

    public void addListener(MapChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MapChangeListener listener) {
        listeners.remove(listener);
    }

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

    @Override
    public void addAnimal(Animal animal) {
        animals.add(animal);
        fields.get(animal.getPosition()).add(animal);
        mapChanged("New animal on tile %s".formatted(animal.getPosition()));
    }

    @Override
    public void removeDeadAnimals() {
        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            if (animal.getEnergy() <= 0) {
                iterator.remove();
                animal.setIsDead(true);
                animal.setDeathDate(daysElapsed);
                fields.get(animal.getPosition()).remove(animal); // removing from fields too
                mapChanged("Animal died on tile %s".formatted(animal.getPosition()));
            }
        }
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
            mapChanged("Plant eaten on tile %s".formatted(v));
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
            mapChanged("New child on tile %s".formatted(v));
        }
    }

    @Override
    public void addPlant(Vector2d position, Plant plant) {
        if (plants.containsKey(position)) {
            throw new IllegalArgumentException("Position %s already has a plant".formatted(position));
        }
        plants.put(position,plant);
        mapChanged("New plant on tile %s".formatted(position));
    }

    @Override
    public void growPlants() {
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

        // Sadzenie w dżungli (80% z config.dailyPlantGrowth())
        // TODO: Do przejrzenia bo chyba się powtarza kod
        int toGrowInJungle = config.dailyPlantGrowth() * 8 / 10;
        int grownInJungle = 0;
        for (int i = 0; i < Math.min(toGrowInJungle, jungleFree.size()); i++) {
            Plant plant = new Plant(ThreadLocalRandom.current().nextInt(
                    config.plantMinEnergy(), config.plantMaxEnergy() + 1)
            );

            addPlant(jungleFree.get(i), plant);
            grownInJungle++;
        }

        // Reszta na stepie
        int toGrowOnStep = config.dailyPlantGrowth() - grownInJungle;
        for (int i = 0; i < Math.min(toGrowOnStep, stepFree.size()); i++) {
            Plant plant = new Plant(ThreadLocalRandom.current().nextInt(
                    config.plantMinEnergy(), config.plantMaxEnergy() + 1)
            );

            addPlant(stepFree.get(i), plant);
        }
    }

    @Override
    public void dayPassed() {
        mapChanged("%s day passed".formatted(daysElapsed));

        daysElapsed++;
        for (Animal animal : animals) {
            animal.setEnergy(animal.getEnergy() - config.animalEnergyLoss());
            animal.setAge(animal.getAge() + 1);
            mapChanged("Animal's energy decreased to %s and age increased to %s"
                    .formatted(animal.getEnergy(),animal.getAge()));
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
