package app.simulation;

public record Statistics(
    int animalsCount,
    int plantsCount,
    int freeFields,
    double averageEnergy,
    double averageLifespan,
    double averageChildrenCount,
    int maxEnergy
) {
}
