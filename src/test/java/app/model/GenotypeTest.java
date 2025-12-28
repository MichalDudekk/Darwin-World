package app.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenotypeTest {
    @Test
    void nextGeneCyclesCorrectly() {
        // when
        Genotype genotype = new Genotype(new ArrayList<>(List.of(0, 4, 7)));

        // then
        assertEquals(0, genotype.getActiveGene());
        genotype.nextGene();
        assertEquals(4, genotype.getActiveGene());
        genotype.nextGene();
        assertEquals(7, genotype.getActiveGene());
        genotype.nextGene();
        assertEquals(0, genotype.getActiveGene());
    }

    @Test
    void crossoverRespectsEnergyProportions() {
        // when
        Genotype strong = new Genotype(new ArrayList<>(List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
        Genotype weak = new Genotype(new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));

        // 80% energii dla silniejszego przy 10 genach powinno dać 8 genów silniejszego
        Genotype child = Genotype.crossover(strong, weak, 80, 20);
        int onesCount = 0;
        for (int i = 0; i < 10; i++) {
            if (child.getActiveGene() == 1) onesCount++;
            child.nextGene();
        }

        // then
        assertEquals(8, onesCount);
    }

    @Test
    void crossoverPerformsDeepCopy() {
        // when
        Genotype parent1 = new Genotype(new ArrayList<>(List.of(0, 0, 0, 0)));
        Genotype parent2 = new Genotype(new ArrayList<>(List.of(0, 0, 0, 0)));
        Genotype child = Genotype.crossover(parent1, parent2, 50, 50);

        // Mutujemy dziecko (100% szansy) - rodzice powinni zostać nietknięci
        child.mutate(1, 1);

        // then
        assertEquals(0, parent1.getActiveGene());
        parent1.nextGene();
        assertEquals(0, parent1.getActiveGene());

        assertEquals(0, parent2.getActiveGene());
        parent2.nextGene();
        assertEquals(0, parent2.getActiveGene());
    }

    @Test
    void mutateChangesGenesWhenChanceIsFull() {
        // when
        Genotype genotype = new Genotype(new ArrayList<>(List.of(0, 0, 0, 0, 0)));

        // 100% szansy na mutację
        genotype.mutate(1, 3);

        int changedGenes = 0;
        for (int i = 0; i < 5; i++) {
            if (genotype.getActiveGene() != 0) changedGenes++;
            genotype.nextGene();
        }

        // then
        assertTrue(changedGenes > 0);
    }

    @Test
    void mutateDoesNotChangeGenesWhenChanceIsZero() {
        // when
        Genotype genotype = new Genotype(new ArrayList<>(List.of(5, 5, 5)));

        // 0% szansy na mutację
        genotype.mutate(0, 0);

        // then
        assertEquals(5, genotype.getActiveGene());
        genotype.nextGene();
        assertEquals(5, genotype.getActiveGene());
        genotype.nextGene();
        assertEquals(5, genotype.getActiveGene());
    }
}