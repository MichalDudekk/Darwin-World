package app.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.List;

class AnimalTest {
    @Test
    void animalEatsPlantAndIncreasesEnergy() {
        // given
        Animal animal = new Animal(10, new Vector2d(2, 2), MapDirection.NORTH,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        Plant plant = new Plant(5);

        // when
        animal.eat(plant);

        // then
        assertEquals(15, animal.getEnergy());
    }

    @Test
    void compareToOrdersByEnergyThenAgeThenChildren() {
        // given
        Animal cow1 = new Animal(20, new Vector2d(0, 0), MapDirection.NORTH, new Genotype(List.of(0)), null);
        cow1.setAge(2);

        Animal cow2 = new Animal(20, new Vector2d(0, 0), MapDirection.NORTH, new Genotype(List.of(0)), null);
        cow2.setAge(5);

        Animal cow3 = new Animal(30, new Vector2d(0, 0), MapDirection.NORTH, new Genotype(List.of(0)), null);

        // then
        assertTrue(cow3.compareTo(cow1) > 0, "Stronger energy should be greater");
        assertTrue(cow2.compareTo(cow1) > 0, "Older age should be greater if energy is equal");
        assertTrue(cow1.compareTo(cow3) < 0, "Weaker energy should be smaller");
    }

    @Test
    void reproduceCreatesChildWithCorrectEnergyAndLinks() {
        // given
        Animal parent1 = new Animal(30, new Vector2d(1, 1), MapDirection.NORTH,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        Animal parent2 = new Animal(30, new Vector2d(1, 1), MapDirection.SOUTH,
                new Genotype(List.of(0)), new StandardMoveStrategy());
        int energyLoss = 10;

        // when
        Animal child = parent1.reproduce(parent2,energyLoss);

        // then
        assertEquals(20, parent1.getEnergy(), "Parent1 should lose %s energy".formatted(energyLoss));
        assertEquals(20, parent2.getEnergy(), "Parent2 should lose %s energy".formatted(energyLoss));
        assertEquals(20, child.getEnergy());
        assertTrue(parent1.getChildren().contains(child));
        assertTrue(parent2.getChildren().contains(child));
        assertEquals(1, parent1.getChildrenCount());
    }

    @Test
    void descendantsCountHandlesCommonDescendantsCorrectly() {
        // given
        // A & B are parents of C & D. C & D are parents of E.
        // A should have 3 descendants
        Animal a = new Animal(100, new Vector2d(0,0), MapDirection.NORTH, new Genotype(List.of(0)), null);
        Animal b = new Animal(100, new Vector2d(0,0), MapDirection.NORTH, new Genotype(List.of(0)), null);

        Animal c = a.reproduce(b,10);
        Animal d = a.reproduce(b,10);

        Animal e = c.reproduce(d,10);

        // then
        assertEquals(2, a.getChildrenCount(), "A has 2 direct children");
        assertEquals(3, a.getDescendantsCount(), "A has 3 unique descendants (C, D, E)");
    }

    @Test
    void rotateByGenesChangesDirectionCorrectly() {
        // given
        // Assuming MapDirection.rotate() works correctly
        // MapDirection.rotate(MapDirection.NORTH) should return MapDirection.EAST
        Animal animal = new Animal(10, new Vector2d(0,0), MapDirection.NORTH,
                new Genotype(List.of(2)), null);

        // when
        animal.rotateByGenes();

        // then
        assertEquals(MapDirection.EAST, animal.getDirection());
    }
}