package app.presenter;

import app.model.MapChangeListener;
import app.model.WorldMap;
import app.simulation.Simulation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

public class SimulationController implements MapChangeListener {
    private static final int CELL_SIZE = 20;

    @FXML Canvas simulationCanvas;

    private Simulation simulation;
    private WorldMap map;

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.map = simulation.getMap();

        simulation.addListener(this);

        // Rysowanie stanu początkowego
//        drawMap();
    }


    @Override
    public void onMapChanged(WorldMap map, String message) {
        Platform.runLater(() -> {
//            drawMap();
//            updateStats();
        });
    }
}
