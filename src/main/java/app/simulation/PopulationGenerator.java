package app.simulation;

import app.model.*;
import app.shared.Config;
import java.util.*;

public class PopulationGenerator {
    public static List<Animal> generate(Config config) {
        List<Animal> animals = new ArrayList<>();
        MoveStrategy strategy = config.isFomo() ?
                new FomoMoveStrategy(config.fomoRadius(), config.fomoCrowdSize()) :
                new StandardMoveStrategy();
        Random rand = new Random();

        for(int i=0; i<config.initialAnimalsNum(); i++) {
            List<Integer> genes = rand.ints(config.genomLength(), 0, 8).boxed().toList();
            animals.add(new Animal(
                    config.animalStartEnergy(),
                    Vector2d.getRandom(0, config.mapWidth()-1, 0, config.mapHeight()-1),
                    MapDirection.getRandom(),
                    new Genotype(genes),
                    strategy
            ));
        }
        return animals;
    }
}