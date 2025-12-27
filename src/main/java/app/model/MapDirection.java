package app.model;

public enum MapDirection {
    NORTH(0, new Vector2d(0, 1)),
    NORTH_EAST(1, new Vector2d(1, 1)),
    EAST(2, new Vector2d(1, 0)),
    SOUTH_EAST(3, new Vector2d(1, -1)),
    SOUTH(4, new Vector2d(0, -1)),
    SOUTH_WEST(5, new Vector2d(-1, -1)),
    WEST(6, new Vector2d(-1, 0)),
    NORTH_WEST(7, new Vector2d(-1, 1));

    private final int value;
    private final Vector2d unitVector;
    private static final MapDirection[] VALUES = values();

    MapDirection(int value, Vector2d unitVector) {
        this.value = value;
        this.unitVector = unitVector;
    }

    private MapDirection rotation(int number) {
        MapDirection[] directions = MapDirection.values();
        return directions[ (this.value + number) % directions.length ];
    }

    public MapDirection next() {
        return rotation(1);
    }

    public MapDirection previous() {
        return rotation(7);
    }

    public MapDirection opposite() {
        return rotation(4);
    }

    public MapDirection rotate(int gene) {
        return rotation(gene);
    }

    public static MapDirection getRandom() {
        int randomInt = (int) (Math.random() * 8);
        return MapDirection.values()[randomInt];
    }

    public Vector2d toUnitVector() {
        return unitVector;
    }
}