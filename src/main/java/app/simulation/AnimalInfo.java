package app.simulation;

import app.model.Vector2d;

import java.util.UUID;

public record AnimalInfo(Vector2d position,
                         int energy,
                         String genotype,
                         UUID id,
                         int activeGene,
                         int plantsEaten,
                         int childrenCount,
                         int descendantsCount,
                         int daysLived
) {
}
