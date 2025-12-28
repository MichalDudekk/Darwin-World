package app.model;

import app.shared.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FomoMoveStrategyTest {
    private GlobeMap map;
    private MoveStrategy fomoStrategy;

    private final int RADIUS = 2;
    private final int MIN_CROWD = 3;

    @BeforeEach
    void setUp() {
        // Initialize strategy and an empty map
        fomoStrategy = new FomoMoveStrategy(RADIUS, MIN_CROWD);
        map = new GlobeMap(10, 10, new ArrayList<>());
    }

    @Test
    void shouldMoveStandardWhenCrowdIsTooSmall() {
        // Given: Only 2 animals (including subject) which is less than MIN_CROWD (3)
        Animal subject = new Animal(20, new Vector2d(5, 5), MapDirection.NORTH,
                new Genotype(List.of(0)), fomoStrategy);
        Animal neighbor = new Animal(20, new Vector2d(5, 6), MapDirection.NORTH,
                new Genotype(List.of(0)), null);

        map.addAnimal(subject);
        map.addAnimal(neighbor);

        // When: Subject decides next move
        Vector2d nextMove = subject.getDesirableMove(map);

        // Then: Should fall back to StandardMoveStrategy (move North)
        // (5,5) + NORTH(0,1) = (5,6)
        assertEquals(new Vector2d(5, 6), nextMove, "Should move forward when crowd is too small");
    }

    @Test
    void shouldMoveTowardsCrowdCenterIncludingSubjectInCalculations() {
        // Given: Subject at (2,2) and 3 neighbors
        // Neighbors: (5,5), (6,5), (7,5)
        // Center = (20/4, 17/4) = (5, 4)

        Animal subject = new Animal(20, new Vector2d(2, 2), MapDirection.NORTH,
                new Genotype(List.of(0)), fomoStrategy);

        MoveStrategy longRangeFomo = new FomoMoveStrategy(10, 4);
        subject.setMoveStrategy(longRangeFomo);

        map.addAnimal(subject);
        map.addAnimal(new Animal(10, new Vector2d(5, 5), MapDirection.NORTH, new Genotype(List.of(0)), null));
        map.addAnimal(new Animal(10, new Vector2d(6, 5), MapDirection.NORTH, new Genotype(List.of(0)), null));
        map.addAnimal(new Animal(10, new Vector2d(7, 5), MapDirection.NORTH, new Genotype(List.of(0)), null));

        // When
        Vector2d nextMove = subject.getDesirableMove(map);

        // Then: Target center is (5,4).
        // followTarget from (2,2) to (5,4) -> x: 2 < 5 (1), y: 2 < 4 (1) -> vector (1,1)
        // (2,2) + (1,1) = (3,3)
        assertEquals(new Vector2d(3, 3), nextMove, "Should move towards (5,4) which is the center including subject");
    }

    @Test
    void shouldMoveStandardIfSubjectIsAlreadyAtCenter() {
        // Given: 3 animals forming a crowd where the center is exactly where the subject stands
        // Neighbors at (4,5) and (6,5). Subject at (5,5). Center = (15/3, 15/3) = (5,5)
        Animal subject = new Animal(20, new Vector2d(5, 5), MapDirection.NORTH,
                new Genotype(List.of(0)), fomoStrategy);

        map.addAnimal(subject);
        map.addAnimal(new Animal(10, new Vector2d(4, 5), MapDirection.NORTH, new Genotype(List.of(0)), null));
        map.addAnimal(new Animal(10, new Vector2d(6, 5), MapDirection.NORTH, new Genotype(List.of(0)), null));

        // When: Subject decides next move
        Vector2d nextMove = subject.getDesirableMove(map);

        // Then: center.equals(position) is true, should move standard (North)
        assertEquals(new Vector2d(5, 6), nextMove, "Should move forward if already at the crowd center");
    }
}