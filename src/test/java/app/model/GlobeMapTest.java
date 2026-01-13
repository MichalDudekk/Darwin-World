package app.model;

import static org.junit.jupiter.api.Assertions.*;

import app.shared.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class GlobeMapTest {
    private GlobeMap map;

    @BeforeEach
    void setUp() {
        Config testConfig = new Config(
                10, 10,
                0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, // Inne parametry (nieistotne dla testu mapy/ruchu)
                false, 0, 0
        );

        // Initialize map with 10x10 dimensions for consistent testing
        map = new GlobeMap(testConfig, new ArrayList<>());
    }

    @Test
    void animalShouldWrapAroundXAxisMovingEast() {
        // Given: Animal at the eastern edge facing East
        Animal cow = new Animal(20, new Vector2d(9, 5), MapDirection.EAST,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        map.addAnimal(cow);

        // When: Animal moves
        map.moveAnimals();

        // Then: Position should wrap to x=0
        assertEquals(new Vector2d(0, 5), cow.getPosition(), "Animal should wrap from x=9 to x=0");
    }

    @Test
    void animalShouldWrapAroundXAxisMovingWest() {
        // Given: Animal at the western edge facing West
        Animal cow = new Animal(20, new Vector2d(0, 5), MapDirection.WEST,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        map.addAnimal(cow);

        // When: Animal moves
        map.moveAnimals();

        // Then: Position should wrap to x=9
        assertEquals(new Vector2d(9, 5), cow.getPosition(), "Animal should wrap from x=0 to x=9");
    }

    @Test
    void animalShouldBounceOffNorthPoleAndReverseDirection() {
        // Given: Animal at the top row facing North
        Animal cow = new Animal(20, new Vector2d(5, 9), MapDirection.NORTH,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        map.addAnimal(cow);

        // When: Animal moves
        map.moveAnimals();

        // Then: Animal stays on the same row but faces South
        assertEquals(new Vector2d(5, 9), cow.getPosition(), "Animal should not exceed height-1");
        assertEquals(MapDirection.SOUTH, cow.getDirection(), "Direction should be reversed at North Pole");
    }

    @Test
    void animalShouldBounceOffSouthPoleAndReverseDirection() {
        // Given: Animal at the bottom row facing South
        Animal cow = new Animal(20, new Vector2d(5, 0), MapDirection.SOUTH,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        map.addAnimal(cow);

        // When: Animal moves
        map.moveAnimals();

        // Then: Animal stays on y=0 but faces North
        assertEquals(new Vector2d(5, 0), cow.getPosition(), "Animal should not go below y=0");
        assertEquals(MapDirection.NORTH, cow.getDirection(), "Direction should be reversed at South Pole");
    }

    @Test
    void removeDeadAnimalsShouldClearAnimalsWithZeroOrLessEnergy() {
        // Given: One healthy and one starving animal
        Animal healthy = new Animal(10, new Vector2d(1, 1), MapDirection.NORTH, new Genotype(List.of(0)), null);
        Animal starving = new Animal(0, new Vector2d(2, 2), MapDirection.NORTH, new Genotype(List.of(0)), null);

        map.addAnimal(healthy);
        map.addAnimal(starving);

        // When: Cleaning up dead animals
        map.removeDeadAnimals();

        // Then: Only healthy animal remains
        assertEquals(1, map.getAnimals().size());
        assertTrue(map.getFields().get(new Vector2d(2,2)).isEmpty(), "Field list should be updated");
    }

    @Test
    void animalsConsumeShouldPrioritizeStrongestAnimal() {
        // Given: Two animals on the same tile with a plant
        Vector2d pos = new Vector2d(3, 3);
        Animal weak = new Animal(10, pos, MapDirection.NORTH, new Genotype(List.of(0)), null);
        Animal strong = new Animal(50, pos, MapDirection.NORTH, new Genotype(List.of(0)), null);

        map.addAnimal(weak);
        map.addAnimal(strong);
        map.addPlant(pos, new Plant(15));

        // When: Consumption phase occurs
        map.animalsConsume();

        // Then: Strongest animal gets the energy and plant disappears
        assertEquals(65, strong.getEnergy(), "Strongest should eat the plant");
        assertEquals(10, weak.getEnergy(), "Weakest should not eat");
        assertFalse(map.getPlants().containsKey(pos), "Plant should be removed from map");
    }

    @Test
    void reproductionShouldOccurOnlyIfEnergyIsSufficient() {
        // Given: Two animals on same tile, one below reproduction threshold
        Vector2d pos = new Vector2d(4, 4);
        Animal parent1 = new Animal(99, pos, MapDirection.NORTH, new Genotype(List.of(0,0)), new StandardMoveStrategy());
        Animal parent2 = new Animal(1, pos, MapDirection.NORTH, new Genotype(List.of(0,0)), new StandardMoveStrategy());

        map.addAnimal(parent1);
        map.addAnimal(parent2);

        // When: Reproduction phase occurs
        map.animalsReproduce();

        // Then
        assertEquals(2, map.getAnimals().size(), "No child should be born if a parent is too weak");
    }
}