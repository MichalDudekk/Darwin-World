package app.model;

public interface MoveStrategy {
    public Vector2d decideNextMove(Animal animal, WorldMap map);
}
