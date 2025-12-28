package app.model;

import java.util.Objects;

public class Vector2d {
    private final int x;
    private final int y;

    public Vector2d(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public String toString() {
        return "(%s, %s)".formatted(x, y);
    }

    public boolean precedes(Vector2d other) {
        return this.x <= other.x && this.y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return this.x >= other.x && this.y >= other.y;
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d( this.x + other.x , this.y + other.y );
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d( this.x - other.x , this.y - other.y );
    }

    public double calculateDistance(Vector2d o) {
        int dx = x - o.getX();
        int dy = y - o.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2d followTarget(Vector2d target) {
        int resX = x < target.getX() ? 1 : x > target.getX() ? -1 : 0;
        int resY = y < target.getY() ? 1 : y > target.getY() ? -1 : 0;

        return new Vector2d(resX,resY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Vector2d)) return false;

        Vector2d that = (Vector2d) other;
        return this.x == that.x && this.y == that.y;
    }
}
