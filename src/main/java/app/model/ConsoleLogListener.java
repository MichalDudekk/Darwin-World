package app.model;

public class ConsoleLogListener implements MapChangeListener{

    @Override
    public void onMapChanged(WorldMap map, String message) {
        System.out.println(message);
    }
}
