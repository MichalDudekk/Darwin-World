package app.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WorldMap {
    public void addAnimal(Animal animal);

    public void removeDeadAnimals();

    public void moveAnimals();

    public void animalsConsume();

    public void animalsReproduce();

    public void growPlants();

    public List<Animal> getAnimals();

    public Map<Vector2d, Plant> getPlants();

    public Map<Vector2d, List<Animal>> getFields();

    public List<Animal> getAnimalsInRadius(Vector2d center, int radius);

    public void addPlant(Vector2d position, Plant plant);

    public void dayPassed();
}
