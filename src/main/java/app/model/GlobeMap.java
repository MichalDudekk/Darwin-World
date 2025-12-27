package app.model;

import java.util.*;

public class GlobeMap extends AbstractWorldMap implements MoveValidator{
    @Override
    public void addAnimal(Animal animal) {
        animals.add(animal);
        fields.get(animal.getPosition()).add(animal);
    }

    @Override
    public void removeDeadAnimals() {
        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            if (animal.getIsDead()) {
                iterator.remove();
                animal.setIsDead(true);
                animal.setDeathDate(daysElapsed);
                fields.get(animal.getPosition()).remove(animal); // removing from fields too
            }
        }
    }

    @Override
    public void moveAnimals() {
        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();

            animal.rotateByGenes();
            Vector2d prevPosition = animal.getPosition();
            Vector2d desirableMove = animal.getDesirableMove(this);
            MoveResult res = validateMove(prevPosition,desirableMove,animal.getDirection());

            animal.setDirection(res.direction());
            animal.setPosition(res.position());

            fields.get(prevPosition).remove(animal);
            fields.get(res.position()).add(animal);
        }
    }

    @Override
    public void animalsConsume() {
        Vector2d[] vectors = plants.keySet().toArray(new Vector2d[0]);

        for (Vector2d v : vectors) {
            List<Animal> residents = fields.get(v);
            if (residents.isEmpty()) continue;

            residents.sort(Comparator.reverseOrder()); // sortowanie malejące
            residents.getFirst().eat(plants.get(v));
            plants.remove(v);
        }
    }

    // do edita - wymaga danych z params
    @Override
    public void animalsReproduce() {
        int MIN_MUTATION = 2;
        int MAX_MUTATION = 4;
        int MIN_ENERGY_TO_REPRODUCE = 10;

        Vector2d[] vectors = fields.keySet().toArray(new Vector2d[0]);

        for (Vector2d v : vectors) {
            List<Animal> residents = fields.get(v);
            if (residents.size() <= 1)
                continue;
            residents.sort(Comparator.reverseOrder()); // sortowanie malejące

            if (residents.get(1).getEnergy() < MIN_ENERGY_TO_REPRODUCE)
                continue;

            Animal child = residents.get(0).reproduce(residents.get(1));
            child.getGenotype().mutate(MIN_MUTATION,MAX_MUTATION);
            this.addAnimal(child);
        }
    }

    @Override
    public void addPlant(Vector2d position, Plant plant) {
        if (plants.containsKey(position)) {
            throw new IllegalArgumentException("Position %s already has a plant".formatted(position));
        }
        plants.put(position,plant);
    }

    // wymaga danych z params do edita
    @Override
    public void growPlants() {
        int DAILY_PLANT_GROWTH = 10;
        int PLANT_ENERGY_VALUE = 5;

        List<Vector2d> jungleFree = new ArrayList<>();
        List<Vector2d> stepFree = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Vector2d v = new Vector2d(x, y);
                if (plants.containsKey(v)) continue;

                if (y >= jungleBottomY && y <= jungleTopY) {
                    jungleFree.add(v);
                } else {
                    stepFree.add(v);
                }
            }
        }

        Collections.shuffle(jungleFree);
        Collections.shuffle(stepFree);

        // Sadzenie w dżungli (80% z DAILY_PLANT_GROWTH)
        int toGrowInJungle = DAILY_PLANT_GROWTH * 8 / 10;
        int grownInJungle = 0;
        for (int i = 0; i < Math.min(toGrowInJungle, jungleFree.size()); i++) {
            addPlant(jungleFree.get(i), new Plant(PLANT_ENERGY_VALUE));
            grownInJungle++;
        }

        // Reszta na stepie
        int toGrowOnStep = DAILY_PLANT_GROWTH - grownInJungle;
        for (int i = 0; i < Math.min(toGrowOnStep, stepFree.size()); i++) {
            addPlant(stepFree.get(i), new Plant(PLANT_ENERGY_VALUE));
        }
    }

    // do edita - wymaga danych z params
    @Override
    public void dayPassed() {
        int ENERGY_LOSS = 1;

        daysElapsed++;
        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            animal.setEnergy(animal.getEnergy() - ENERGY_LOSS);
            animal.setAge(animal.getAge() + 1);
        }
    }

    @Override
    public List<Animal> getAnimalsInRadius(Vector2d center, int radius) {
        List<Animal> res = new ArrayList<>();

        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            double distance = center.calculateDistance(animal.getPosition());

            if (distance <= radius) {
                res.add(animal);
            }
        }
        return res;
    }

    @Override
    public MoveResult validateMove(Vector2d oldPosition, Vector2d intendedMove, MapDirection orientation) {
        int intendedX = intendedMove.getX();
        int intendedY = intendedMove.getY();

        if (0 <= intendedX && intendedX < width && 0 <= intendedY && intendedY < height) {
            return new MoveResult(intendedMove,orientation);
        }

        int newX = intendedX;
        int newY = intendedY;

        if (intendedX < 0) {
            newX = width-1;
        } else if (intendedX >= width) {
            newX = 0;
        }

        if (intendedY < 0) {
            newY = 1;
            orientation = orientation.opposite();
        } else if (intendedY >= height) {
            newY = height-1;
            orientation = orientation.opposite();
        }

        return new MoveResult(new Vector2d(newX,newY),orientation);
    }
}
