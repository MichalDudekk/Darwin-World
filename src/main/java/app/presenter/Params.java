package app.presenter;

public record Params(
        int MapWidth,
        int mapHeigth,
        int initialPlantsNum,
        int plantMinEnergy,
        int planMaxEnergy,
        int dailyPlantGrowth,
        int initalAnimalsNum,
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
