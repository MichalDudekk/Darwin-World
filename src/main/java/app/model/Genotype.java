package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Genotype {
    private List<Integer> genes;
    private int activeGene;
    private int currentIndex = 0;
    private static final int POSSIBLE_GENES_COUNT = MapDirection.getAllDirectionsCount();

    public Genotype(List<Integer> genes) {
        this.genes = new ArrayList<>(genes);
        activeGene = this.genes.getFirst();
    }

    public int getActiveGene() {
        return activeGene;
    }

    public void nextGene() {
        currentIndex = (currentIndex + 1) % genes.size();
        activeGene = this.genes.get(currentIndex);
    }

    public static Genotype crossover(Genotype gen1, Genotype gen2, int energy1, int energy2) {
        boolean p1Stronger = energy1 >= energy2;
        List<Integer> strongerGenes = p1Stronger ? gen1.genes : gen2.genes;
        List<Integer> weakerGenes = p1Stronger ? gen2.genes : gen1.genes;

        int totalEnergy = energy1 + energy2;
        double ratio = totalEnergy > 0 ? (double) Math.max(energy1, energy2) / totalEnergy : 0.5;

        int length = strongerGenes.size();
        int splitPoint = (int) (ratio * length);

        List<Integer> childGenes = new ArrayList<>();

        boolean strongerOnLeft = new Random().nextBoolean();

        if (strongerOnLeft) {
            // Silniejszy z lewej
            childGenes.addAll(strongerGenes.subList(0, splitPoint));
            childGenes.addAll(weakerGenes.subList(splitPoint, length));
        } else {
            // Silniejszy z prawej
            childGenes.addAll(weakerGenes.subList(0, length - splitPoint));
            childGenes.addAll(strongerGenes.subList(length - splitPoint, length));
        }

        return new Genotype(childGenes);
    }

    public void mutate(int minMutation, int maxMutation) {
        Random random = new Random();

        int mutationCount = random.nextInt(maxMutation - minMutation + 1) + minMutation;

        List<Integer> indexes = IntStream.range(0, genes.size())
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(indexes);

        for (int i = 0; i < Math.min(mutationCount, genes.size()); i++) {
            int index = indexes.get(i);
            int currentGene = genes.get(index);

            int randomStep = 1 + random.nextInt(POSSIBLE_GENES_COUNT - 1);
            int newGene = (currentGene + randomStep) % POSSIBLE_GENES_COUNT;

            genes.set(index, newGene);
        }
    }

    @Override
    public String toString() {
        return genes.toString();
    }
}
