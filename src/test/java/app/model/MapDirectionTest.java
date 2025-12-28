package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapDirectionTest {
    @Test
    void nextReturnsCorrectValue() {
        // when
        MapDirection north = MapDirection.NORTH;
        MapDirection east = MapDirection.EAST;
        MapDirection south = MapDirection.SOUTH;
        MapDirection northWest = MapDirection.NORTH_WEST;

        // then
        assertEquals(MapDirection.NORTH_EAST, north.next());
        assertEquals(MapDirection.SOUTH_EAST, east.next());
        assertEquals(MapDirection.SOUTH_WEST, south.next());
        assertEquals(MapDirection.NORTH, northWest.next());
    }

    @Test
    void previousReturnsCorrectValue() {
        // when
        MapDirection north = MapDirection.NORTH;
        MapDirection east = MapDirection.EAST;
        MapDirection south = MapDirection.SOUTH;
        MapDirection northEast = MapDirection.NORTH_EAST;

        // then
        assertEquals(MapDirection.NORTH_WEST, north.previous());
        assertEquals(MapDirection.NORTH_EAST, east.previous());
        assertEquals(MapDirection.SOUTH_EAST, south.previous());
        assertEquals(MapDirection.NORTH, northEast.previous());
    }

    @Test
    void oppositeReturnsCorrectValue() {
        // when
        MapDirection north = MapDirection.NORTH;
        MapDirection northEast = MapDirection.NORTH_EAST;
        MapDirection east = MapDirection.EAST;
        MapDirection southWest = MapDirection.SOUTH_WEST;

        // then
        assertEquals(MapDirection.SOUTH, north.opposite());
        assertEquals(MapDirection.SOUTH_WEST, northEast.opposite());
        assertEquals(MapDirection.WEST, east.opposite());
        assertEquals(MapDirection.NORTH_EAST, southWest.opposite());
    }

    @Test
    void rotateWithGeneReturnsCorrectValue() {
        // when
        MapDirection north = MapDirection.NORTH;

        // then
        assertEquals(MapDirection.NORTH, north.rotate(0));
        assertEquals(MapDirection.SOUTH, north.rotate(4));
        assertEquals(MapDirection.WEST, north.rotate(6));
        assertEquals(MapDirection.NORTH_EAST, north.rotate(1));
    }

    @Test
    void toUnitVectorReturnsCorrectVector() {
        // when
        MapDirection north = MapDirection.NORTH;
        MapDirection southEast = MapDirection.SOUTH_EAST;
        MapDirection west = MapDirection.WEST;

        // then
        assertEquals(new Vector2d(0, 1), north.toUnitVector());
        assertEquals(new Vector2d(1, -1), southEast.toUnitVector());
        assertEquals(new Vector2d(-1, 0), west.toUnitVector());
    }
}