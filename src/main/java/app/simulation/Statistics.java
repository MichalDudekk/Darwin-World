package app.simulation;

import java.util.List;
import java.util.Map;

public record Statistics(
    int animalsCount,
    int plantsCount,
    int freeFields,
    double averageEnergy,
    double averageLifespan,
    double averageChildrenCount,
    int maxEnergy,
    List<Map.Entry<String, Integer>> topGenotypes
) {
}
