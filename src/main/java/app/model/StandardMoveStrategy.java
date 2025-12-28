package app.model;

public class StandardMoveStrategy implements MoveStrategy{

    @Override
    public Vector2d decideNextMove(Animal animal, WorldMap map) {
        MapDirection direction = animal.getDirection();
        return animal.getPosition().add(direction.toUnitVector()); // animal moves forward
    }
}
