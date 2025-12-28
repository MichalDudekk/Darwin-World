package app.presenter;

import app.model.*;
import app.shared.Config;
import app.simulation.Simulation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SimulationController implements MapChangeListener {
    private static final int CELL_SIZE = 20;

    @FXML Canvas simulationCanvas;

    private double cellWidth;
    private double cellHeight;

    private Simulation simulation;
    private WorldMap map;
    private Config config;

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.map = simulation.getMap();

        simulation.addListener(this);

        // Rysowanie stanu początkowego
//        drawMap();
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void onMapChanged(WorldMap map, String message) {
        Platform.runLater(() -> {
            drawSystem();
//            updateStats();
        });
    }

    private void drawSystem() {
        GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
        double width = simulationCanvas.getWidth();
        double height = simulationCanvas.getHeight();

        this.cellWidth = width / config.mapWidth();
        this.cellHeight = height / config.mapHeight();

        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(0, 0, width, height);

        for (Animal animal : map.getAnimals()) {
            int x = animal.getPosition().getX();
            int y = animal.getPosition().getY();
            int energy = animal.getEnergy(); // 0-100

            double hue = (energy / 100.0) * 240.0;
            gc.setFill(Color.hsb(hue, 1.0, 1.0));

            // Rysowanie samego zwierzaka
            gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
        }

        for (Vector2d plantPostion : map.getPlants().keySet()) {
            int x = plantPostion.getX();
            int y = plantPostion.getY();

            gc.setFill(Color.FORESTGREEN);

            // Rysowanie samego zwierzaka
            gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
        }
    }
}
