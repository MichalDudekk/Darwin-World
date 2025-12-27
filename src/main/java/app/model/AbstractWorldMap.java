package app.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractWorldMap implements WorldMap {
    protected int width;
    protected int height;
    protected int jungleBottomY;
    protected int jungleTopY;

    protected Map<Vector2d, List<Animal>> fields;
    protected Map<Vector2d, Plant> plants;
    protected List<Animal> animals = new ArrayList<>();

    protected int daysElapsed = 0;
    protected List<MapChangeListener> listeners = new ArrayList<>();

    public void MapChanged(String message) {
        for (MapChangeListener listener : listeners) {
            listener.onMapChanged(this,message);
        }
    }

    public void AddListener(MapChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MapChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public List<Plant> getPlants() {
        return List.copyOf(plants.values());
    }

    @Override
    public Map<Vector2d, List<Animal>> getFields() {
        return fields;
    }

    @Override
    public List<Animal> getAnimals() {
        return animals;
    }
}
