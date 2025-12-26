package app.simulation;

import java.util.Map;

public record SimulationState(
        int day,
//        Map<Vector2d, List<Animal>>
//        Map<Vector2d, Plant> plantPositions,
        Statistics statistics
) {

}
