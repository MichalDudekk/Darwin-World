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
        }
    }

    @Override
    public void animalsReproduce() {
        Vector2d[] vectors = fields.keySet().toArray(new Vector2d[0]);

        for (Vector2d v : vectors) {
            List<Animal> residents = fields.get(v);
            if (residents.size() <= 1) continue;

            residents.sort(Comparator.reverseOrder()); // sortowanie malejące
            Animal child = residents.get(0).reproduce(residents.get(1));
            this.addAnimal(child);
        }
    }

    // wymaga danych z params do edita
    @Override
    public void growPlants() {
        //
    }

    @Override
    public List<Animal> getAnimalsInRadius(Vector2d center, int radius) {
        List<Animal> res = new LinkedList<>();

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
