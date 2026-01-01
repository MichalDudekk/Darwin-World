package app.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WorldMap {
    void addAnimal(Animal animal);

    List<Animal> removeDeadAnimals();

    void moveAnimals();

    void animalsConsume();

    void animalsReproduce();

    void growPlants(int count);

    void growPlants();

    List<Animal> getAnimals();

    Map<Vector2d, Plant> getPlants();

    int getDaysElapsed();

    Map<Vector2d, List<Animal>> getFields();

    List<Animal> getAnimalsInRadius(Vector2d center, int radius);

    void addPlant(Vector2d position, Plant plant);

    void dayPassed();
}
