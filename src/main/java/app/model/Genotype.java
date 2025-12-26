package app.model;

import java.util.ArrayList;
import java.util.List;

public class Genotype {
    private List<Integer> genes;
    private int activeGene;
    private int currentIndex = 0;
    private static final int POSSIBLE_GENES_COUNT = 8;

    public Genotype(List<Integer> genes) {
        this.genes = genes;
        activeGene = this.genes.get(currentIndex);
    }

    public int getActiveGene() {
        return activeGene;
    }

    public void nextGene() {
        currentIndex = (currentIndex + 1) % genes.size();
        activeGene = this.genes.get(currentIndex);
    }

    public static Genotype crossover(Genotype gen1, Genotype gen2, int energy1, int energy2) {
        List<Integer> genesStronger = energy1 > energy2 ? gen1.genes : gen2.genes;
        List<Integer> genesWeaker = energy1 > energy2 ? gen2.genes : gen1.genes;
        List<Integer> genesChild = new ArrayList<>();

        int energyStronger = Math.max(energy1, energy2);

        int lenght = genesStronger.size();
        int combinedEnergy = energy1 + energy2;
        double proportion = (double) energyStronger / combinedEnergy;

        String side = Math.random() >= 0.5 ? "left" : "right";

        if (side.equals("left")) {
            int breakpoint = (int) (proportion * lenght);

            for (int i=0;i<lenght;i++) {
                if (i <= breakpoint) {
                    genesChild.add(genesStronger.get(i));
                    continue;
                }
                genesChild.add(genesWeaker.get(i));
            }
        } else {
            int breakpoint = lenght - (int) (proportion * lenght);

            for (int i=0;i<lenght;i++) {
                if (i < breakpoint) {
                    genesChild.add(genesWeaker.get(i));
                    continue;
                }
                genesChild.add(genesStronger.get(i));
            }
        }

        return new Genotype(genesChild);
    }

    /**
     * Mutates the genotype based on a mutation probability and a maximum change range.
     *
     * @param mutationChance The probability that a SINGLE gene will undergo mutation.
     * Should be a double in the range [0.0; 1.0].
     * @param maxMutationChange The maximum displacement (delta) for a single gene mutation.
     * The new gene value is randomly selected from the range [prev - max, prev + max],
     * excluding the current value to ensure an actual change occurs.
     * * Example: if maxMutationChange = 2 and prevGene = 5,
     * the newGene will be one of {3, 4, 6, 7} (considering wrap-around for 8 directions).
     */
    public void mutate(double mutationChance, int maxMutationChange) {
        for (int i = 0; i < genes.size(); i++) {
            if (Math.random() > mutationChance) continue;

            int offset = (int) (Math.random() * maxMutationChange) + 1;
            int sign = Math.random() > 0.5 ? -1 : 1;

            int newGene = genes.get(i);
            newGene = (POSSIBLE_GENES_COUNT + newGene + offset * sign) % POSSIBLE_GENES_COUNT;
            genes.set(i,newGene);
        }
    }
}
