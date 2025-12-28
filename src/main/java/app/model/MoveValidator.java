package app.model;

public interface MoveValidator {
    public MoveResult validateMove(Vector2d oldPosition, Vector2d intendedMove, MapDirection orientation);
}
