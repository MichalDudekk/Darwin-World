package app.shared;

public record Params(
        int mapWidth,
        int mapHeight,
        int initialPlantsNum,
        int plantMinEnergy,
        int plantMaxEnergy,
        int dailyPlantGrowth,
        int initialAnimalsNum,
        int animalEnergyLoss,
        int energyNeededToReproduce,
        int energyUsedToReproduce,
        int minMutations,
        int maxMutations,
        int genomLength,
        boolean isFomo,
        int fomoRadius,
        int fomoCrowdSize
) {
}
