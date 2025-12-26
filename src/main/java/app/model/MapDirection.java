package app.model;

public enum MapDirection {
    NORTH(0),
    NORTH_EAST(1),
    EAST(2),
    SOUTH_EAST(3),
    SOUTH(4),
    SOUTH_WEST(5),
    WEST(6),
    NORTH_WEST(7);

    private final int value;

    MapDirection(int value) {
        this.value = value;
    }

    private MapDirection Rotation(int number) {
        MapDirection[] directions = MapDirection.values();
        return directions[ (this.value + number) % directions.length ];
    }

    public MapDirection next() {
        return Rotation(1);
    }

    public MapDirection previous() {
        return Rotation(7);
    }

    public MapDirection opposite() {
        return Rotation(4);
    }

    public MapDirection Rotate(int gene) {
        return Rotation(gene);
    }

    public static MapDirection getRandom() {
        int randomInt = (int) (Math.random() * 8);
        return MapDirection.values()[randomInt];
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH -> new Vector2d(0,1);
            case NORTH_EAST -> new Vector2d(1,1);
            case EAST -> new Vector2d(1,0);
            case SOUTH_EAST -> new Vector2d(1,-1);
            case SOUTH -> new Vector2d(0,-1);
            case SOUTH_WEST -> new Vector2d(-1,-1);
            case WEST -> new Vector2d(-1,0);
            case NORTH_WEST -> new Vector2d(-1,1);
        };
    }
}