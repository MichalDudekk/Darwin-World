package app.simulation;

import app.model.Animal;
import app.model.Plant;
import app.model.Vector2d;

import java.util.List;
import java.util.Map;

public record SimulationState(
        Map<Vector2d, List<Animal>> animalPositions,
        Map<Vector2d, Plant> plantPositions,
        Statistics statistics
) {

}
