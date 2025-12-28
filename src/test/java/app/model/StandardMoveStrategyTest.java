package app.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardMoveStrategyTest {

    @Test
    void shouldMoveNorthWhenFacingNorth() {
        // Given
        StandardMoveStrategy strategy = new StandardMoveStrategy();
        // Animal at (2, 2) facing NORTH
        Animal animal = new Animal(20, new Vector2d(2, 2), MapDirection.NORTH,
                new Genotype(List.of(0)), strategy);

        // When
        Vector2d nextMove = strategy.decideNextMove(animal, null);

        // Then: (2, 2) + NORTH(0, 1) = (2, 3)
        assertEquals(new Vector2d(2, 3), nextMove);
    }

    @Test
    void shouldMoveSouthEastWhenFacingSouthEast() {
        // Given
        StandardMoveStrategy strategy = new StandardMoveStrategy();
        // Animal at (2, 2) facing SOUTH_EAST
        Animal animal = new Animal(20, new Vector2d(2, 2), MapDirection.SOUTH_EAST,
                new Genotype(List.of(0)), strategy);

        // When
        Vector2d nextMove = strategy.decideNextMove(animal, null);

        // Then: (2, 2) + SOUTH_EAST(1, -1) = (3, 1)
        assertEquals(new Vector2d(3, 1), nextMove);
    }

    @Test
    void shouldMoveWestWhenFacingWest() {
        // Given
        StandardMoveStrategy strategy = new StandardMoveStrategy();
        // Animal at (5, 5) facing WEST
        Animal animal = new Animal(20, new Vector2d(5, 5), MapDirection.WEST,
                new Genotype(List.of(0)), strategy);

        // When
        Vector2d nextMove = strategy.decideNextMove(animal, null);

        // Then: (5, 5) + WEST(-1, 0) = (4, 5)
        assertEquals(new Vector2d(4, 5), nextMove);
    }
}