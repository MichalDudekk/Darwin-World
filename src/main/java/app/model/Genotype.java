package app.model;

import java.util.ArrayList;
import java.util.Collections;
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

        int length = genesStronger.size();
        int combinedEnergy = energy1 + energy2;
        double proportion = (double) energyStronger / combinedEnergy;

        String side = Math.random() >= 0.5 ? "left" : "right";

        if (side.equals("left")) {
            int breakpoint = (int) (proportion * length);

            for (int i=0;i<length;i++) {
                if (i < breakpoint) {
                    genesChild.add(genesStronger.get(i));
                    continue;
                }
                genesChild.add(genesWeaker.get(i));
            }
        } else {
            int breakpoint = length - (int) (proportion * length);

            for (int i=0;i<length;i++) {
                if (i < breakpoint) {
                    genesChild.add(genesWeaker.get(i));
                    continue;
                }
                genesChild.add(genesStronger.get(i));
            }
        }

        return new Genotype(genesChild);
    }

    public void mutate(int minMutation, int maxMutation) {
        int mutation = (int) (Math.random() * (maxMutation - minMutation + 1)) + minMutation;

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < genes.size(); i++) {
            indexes.add(i);
        }

        Collections.shuffle(indexes);

        for (int i=0;i<Math.min(mutation,genes.size());i++) {
            int indexToChange = indexes.get(i);

            int currentGene = genes.get(indexToChange);
            // Losuje nową wartość z pozostałych 7 możliwości
            int newGene = (currentGene + 1 + (int)(Math.random() * (POSSIBLE_GENES_COUNT - 1))) % POSSIBLE_GENES_COUNT;
            genes.set(indexToChange,newGene);
        }
    }
}
