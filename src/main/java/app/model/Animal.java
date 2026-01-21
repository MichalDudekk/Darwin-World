package app.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Animal implements Comparable<Animal>{
    private final UUID id = UUID.randomUUID();
    private int energy;
    private int age;
    private Vector2d position;
    private final Set<Animal> children = new HashSet<>();
    private MapDirection direction;
    private final Genotype genotype;
    private int eatenPlants;
    private MoveStrategy moveStrategy;

    public Animal(int energy, Vector2d position,
                  MapDirection direction, Genotype genotype, MoveStrategy moveStrategy) {
        this.energy = energy;
        this.position = position;
        this.direction = direction;
        this.genotype = genotype;
        this.moveStrategy = moveStrategy;
    }

    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public void setMoveStrategy(MoveStrategy moveStrategy) {
        this.moveStrategy = moveStrategy;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public MapDirection getDirection() {
        return direction;
    }

    public void setDirection(MapDirection direction) {
        this.direction = direction;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Set<Animal> getChildren() {
        return children;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public int getEatenPlants() {
        return eatenPlants;
    }

    public Vector2d getDesirableMove(WorldMap map) {
        return moveStrategy.decideNextMove(this, map);
    }

    public void rotateByGenes() {
        direction = direction.rotate(genotype.getActiveGene());
    }

    public void nextGene() {
        genotype.nextGene();
    }

    public void eat(Plant plant) {
        energy += plant.energyValue();
        eatenPlants++;
    }

    public Animal reproduce(Animal partner, int energyLoss) {
        Genotype childGenotype = Genotype.crossover(this.genotype,partner.genotype,this.energy, partner.energy);

        int childEnergy = energyLoss * 2;
        this.decreaseEnergy(energyLoss);
        partner.decreaseEnergy(energyLoss);

        Animal child = new Animal(childEnergy,
                this.position,
                MapDirection.getRandom(),
                childGenotype,
                moveStrategy);
        this.addChildren(child);
        partner.addChildren(child);

        return child;
    }

    public int getDescendantsCount() {
        Set<Animal> allDescendants = new HashSet<>();
        collectDescendants(this, allDescendants);
        return allDescendants.size();
    }

    private void collectDescendants(Animal current, Set<Animal> accumulator) {
        for (Animal child : current.getChildren()) {
            // add() zwraca true jeśli elementu nie było wcześniej w zbiorze
            if (accumulator.add(child)) {
                collectDescendants(child, accumulator);
            }
        }
    }

    public int getChildrenCount() {
        return children.size();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public int compareTo(Animal o) {
        return Comparator.comparingInt(Animal::getEnergy)
                .thenComparingInt(Animal::getAge)
                .thenComparingInt(Animal::getChildrenCount)
                .thenComparing(Animal::getId)
                .compare(this, o);
        // UUID implementuje Comparable - UUID jest praktycznie losowe, więc można przyjąć to jako losowy wybór
        // przy użyciu Math.random() relacja przestałaby być porządkiem (przechodniość)
        // e.g. ~(A>B ^ B>C => A>C)
    }

    public void addChildren(Animal child) {
        children.add(child);
    }

    public void decreaseEnergy(int energyLoss) {
        energy -= energyLoss;
    }
}

