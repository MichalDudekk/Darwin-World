package app.model;

import app.shared.Config;

import java.util.*;

public class GlobeMap extends AbstractWorldMap implements MoveValidator {
    public GlobeMap(Config configuration, List<Animal> animals) {
        config = configuration;
        jungleBottomY = (int) (config.mapHeight() * 0.4);
        jungleTopY = (int) (config.mapHeight() * 0.6);

        this.fields = new HashMap<>();

        for (int x = 0; x < config.mapWidth(); x++) {
            for (int y = 0; y < config.mapHeight(); y++) {
                fields.put(new Vector2d(x, y), new ArrayList<>());
            }
        }

        for (Animal animal : animals) {
            addAnimal(animal);
        }

        this.growPlants(config.initialPlantsNum());
    }

    @Override
    public void moveAnimals() {
        for (Animal animal : animals) {
            animal.rotateByGenes();
            animal.nextGene();
            Vector2d prevPosition = animal.getPosition();
            Vector2d desirableMove = animal.getDesirableMove(this);
            MoveResult res = validateMove(prevPosition,desirableMove,animal.getDirection());

            animal.setDirection(res.direction());
            animal.setPosition(res.position());

            fields.get(prevPosition).remove(animal);
            fields.get(res.position()).add(animal);

            mapChanged("Animal moved from %s to %s".formatted(prevPosition,animal.getPosition()));
        }
    }

    @Override
    public MoveResult validateMove(Vector2d oldPosition, Vector2d intendedMove, MapDirection orientation) {
        int intendedX = intendedMove.getX();
        int intendedY = intendedMove.getY();

        if (0 <= intendedX && intendedX < config.mapWidth() && 0 <= intendedY && intendedY < config.mapHeight())
            return new MoveResult(intendedMove,orientation);

        int newX = intendedX < 0 ? config.mapWidth() - 1 : intendedX >= config.mapWidth() ? 0 : intendedX;
        int newY = intendedY < 0 ? 0 : intendedY >= config.mapHeight() ? config.mapHeight() - 1 : intendedY;

        if (newY != intendedY)
            orientation = orientation.opposite();

        return new MoveResult(new Vector2d(newX,newY),orientation);
    }
}
