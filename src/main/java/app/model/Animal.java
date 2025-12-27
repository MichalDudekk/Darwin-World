package app.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Animal implements Comparable<Animal>{
    private final UUID id = UUID.randomUUID();;
    private int energy;
    private int age;
    private Vector2d position;
    private final Set<Animal> children = new HashSet<>();
    private MapDirection direction;
    private Genotype genotype;
    private int deathDate;
    private int eatenPlants;
    MoveStrategy moveStrategy;
    private boolean isTracked = false;
//    private boolean isInFrenzy = false;
    private boolean isDead = false;

    public Animal(int energy, Vector2d position, MapDirection direction, Genotype genotype) {
        this.energy = energy;
        this.position = position;
        this.direction = direction;
        this.genotype = genotype;
    }

    public Vector2d getDesirableMove(WorldMap map) {
        return moveStrategy.decideNextMove(this, map);
    }

    public void rotateByGenes() {
        direction = MapDirection.values()[genotype.getActiveGene()];
        genotype.nextGene();
    }

    public Vector2d getPosition() {
        return position;
    }

    public boolean getIsDead() {
        return isDead;
    }

    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public void setDeathDate(int deathDate) {
        this.deathDate = deathDate;
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

    public void setIsDead(boolean b) {
        isDead = b;
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

    public void eat(Plant plant) {
        energy += plant.energyValue();
        eatenPlants++;
    }

    // zależy od Paramsów - wymaga zmian
    public Animal reproduce(Animal partner) {
        Genotype childGenotype = Genotype.crossover(this.genotype,partner.genotype,this.energy, partner.energy);

        int KONKRETNA_WARTOSC_Z_PARAMS = 10; // pozniej ustawić
        int childEnergy = KONKRETNA_WARTOSC_Z_PARAMS * 2;
        this.energy -= KONKRETNA_WARTOSC_Z_PARAMS;
        partner.energy -= KONKRETNA_WARTOSC_Z_PARAMS;

        Animal child = new Animal(childEnergy,this.position,MapDirection.getRandom(),childGenotype);
        children.add(child);
        partner.getChildren().add(child);

        return child;
    }

//    // move strategy trzeba zmieniać
//    public void setInFrenzy(boolean inFrenzy) {
//        isInFrenzy = inFrenzy;
//    }

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

    @Override
    public int compareTo(Animal o) {
        int res = Integer.compare(this.energy, o.energy);
        if (res != 0) return res;

        res = Integer.compare(this.age, o.age);
        if (res != 0) return res;

        res = Integer.compare(this.getChildren().size(), o.getChildren().size());
        if (res != 0) return res;

        // UUID implementuje Comparable - UUID jest praktycznie losowe, więc można przyjąć to jako losowy wybór
        // przy użyciu Math.random() relacja przestała by być porządkiem (przechodniość)
        // e.g. ~(A>B ^ B>C => A>C)
        return this.id.compareTo(o.id);
    }
}
