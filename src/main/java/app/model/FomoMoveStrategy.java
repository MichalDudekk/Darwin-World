package app.model;

import java.util.List;

public class FomoMoveStrategy extends StandardMoveStrategy{
    int radius;
    int minCrowdSize;

    public FomoMoveStrategy(int radius, int minCrowdSize) {
        this.radius = radius;
        this.minCrowdSize = minCrowdSize;
    }

    @Override
    public Vector2d decideNextMove(Animal animal, WorldMap map) {
        List<Animal> crowd = map.getAnimalsInRadius(animal.getPosition(),radius);
        if (crowd.size() < minCrowdSize)
            return super.decideNextMove(animal,map);

        Vector2d center = calculateCrowdCenter(crowd);
        if (center.equals(animal.getPosition()))
            return super.decideNextMove(animal,map);

        // followTarget returns unit Vector
        Vector2d pathToCenter = animal.getPosition().followTarget(center);
        return animal.getPosition().add(pathToCenter);
    }

    private Vector2d calculateCrowdCenter(List<Animal> crowd) {
        int length = crowd.size();
        int sumX = 0;
        int sumY = 0;

        for (Animal animal : crowd) {
            Vector2d position = animal.getPosition();
            sumX += position.getX();
            sumY += position.getY();
        }

        int centerX = Math.round((float) sumX /length);
        int centerY = Math.round((float) sumY /length);

        return new Vector2d( centerX, centerY );
    }
}
