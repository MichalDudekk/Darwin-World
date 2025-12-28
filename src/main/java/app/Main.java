package app;

import app.model.*;

import java.util.List;

public class Main {
    public static void main() {

//        MoveStrategy moveStrategy = new FomoMoveStrategy(3,5);
        MoveStrategy moveStrategy = new StandardMoveStrategy();
        Animal cow1 = new Animal(15, new Vector2d(4,4),
                MapDirection.NORTH_EAST, new Genotype(List.of(0,0,0)),moveStrategy);
        Animal cow2 = new Animal(15, new Vector2d(4,4),
                MapDirection.getRandom(), new Genotype(List.of(0,0,0)),moveStrategy);
        Animal cow3 = new Animal(15, new Vector2d(4,4),
                MapDirection.getRandom(), new Genotype(List.of(0,0,0)),moveStrategy);
        Animal cow4 = new Animal(15, new Vector2d(4,4),
                MapDirection.getRandom(), new Genotype(List.of(0,0,0)),moveStrategy);

        System.out.println(cow1.getDirection());
        System.out.println(cow2.getDirection());
        System.out.println(cow3.getDirection());
        System.out.println(cow4.getDirection());

        List<Animal> animals = List.of(cow1,cow2,cow3,cow4);
        AbstractWorldMap map = new GlobeMap(10,10,animals);
        map.AddListener(new ConsoleLogListener());

        for (int i=0;i<20;i++) {
            System.out.println("=======================");
            map.removeDeadAnimals();
            map.moveAnimals();
            map.animalsConsume();
            map.animalsReproduce();
            map.growPlants();
            map.dayPassed();
        }
    }
}
