package app.presenter;

import app.model.*;
import app.shared.Config;
import app.simulation.Simulation;
import app.simulation.SimulationState;
import app.simulation.Statistics;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import java.util.List;

public class SimulationController implements MapChangeListener {
    @FXML private Canvas simulationCanvas;
    @FXML private Label statusLabel;
    @FXML private CheckBox showGridBox;
    @FXML private Slider zoomSlider;
    @FXML private Label dayLabel, allAnimalsLabel, allPlantsLabel, freeFieldsLabel, avgEnergyLabel, avgLifespanLabel, avgChildrenCount;

    private double currentZoom = 600.0;

    private double cellWidth;
    private double cellHeight;

    private Simulation simulation;
    private WorldMap map;
    private Config config;

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.map = simulation.getMap();

        simulation.addListener(this);

        if (config != null) {
            drawSystem();
        }
    }

    @FXML
    public void initialize() {
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentZoom = newValue.doubleValue();
            updateCanvasSize();
            drawSystem();
        });

        updateCanvasSize();
    }

    private void updateCanvasSize() {
        simulationCanvas.setWidth(currentZoom);
        simulationCanvas.setHeight(currentZoom);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void onMapChanged(WorldMap map, String message) {
        Platform.runLater(() -> {
            updateStats();
            drawSystem();
        });
    }

    @FXML
    public void onRedrawNeeded() {
        drawSystem();
    }

    private void drawSystem() {
        GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
        double size = currentZoom;

        this.cellWidth = size / config.mapWidth();
        this.cellHeight = size / config.mapHeight();

        // Background fill
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(0, 0, size, size);

        if (showGridBox.isSelected()) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.15));
            gc.setLineWidth(1);
            // Rysujemy linie siatki
            for (double i = 0; i <= size; i += cellWidth) gc.strokeLine(i, 0, i, size);
            for (double i = 0; i <= size; i += cellHeight) gc.strokeLine(0, i, size, i);
        }

        for (Vector2d plantPostion : map.getPlants().keySet()) {
            int x = plantPostion.getX();
            int y = plantPostion.getY();

            gc.setFill(Color.FORESTGREEN);

            // Rysowanie samego zwierzaka
            gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
        }

        for (Animal animal : map.getAnimals()) {
            int x = animal.getPosition().getX();
            int y = animal.getPosition().getY();
            int energy = animal.getEnergy(); // 0-100

            double hue = (energy / 100.0) * 240.0;
            gc.setFill(Color.hsb(hue, 1.0, 1.0));

            // Rysowanie samego zwierzaka
            gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
        }
    }

    private void updateStats() {
        List<SimulationState> stats = simulation.getHistory();
        Statistics currentDayStats = stats.getLast().statistics();

        dayLabel.setText("Dzień: %s".formatted(map.getDaysElapsed()));
        allAnimalsLabel.setText("Zwierzaki: %s".formatted(currentDayStats.animalsCount()));
        allPlantsLabel.setText("Rośliny: %s".formatted(currentDayStats.plantsCount()));
        freeFieldsLabel.setText("Wolne pola: %s".formatted(currentDayStats.freeFields()));
        avgEnergyLabel.setText("Śr. energia: %.2f".formatted(currentDayStats.averageEnergy()));
        avgLifespanLabel.setText("Śr. dł. życia: %.2f".formatted(currentDayStats.averageLifespan()));
        avgChildrenCount.setText("Śr. il. dzieci: %.2f".formatted(currentDayStats.averageChildrenCount()));
    }

    @FXML
    public void onStepBack() {

    }

    @FXML
    private void onPauseClicked() {
        if(simulation != null) simulation.pause();
//        isLive = false; // Przechodzimy w tryb manualny
        statusLabel.setText("PAUZA");
    }

    @FXML
    private void onResumeClicked() {
        if(simulation != null) simulation.resume();
//        isLive = true; // Wracamy do live
        // Przeskakujemy od razu do najnowszego dnia
//        displayedDay = history.size() - 1;
        statusLabel.setText("Symulacja trwa...");
    }

    @FXML
    public void onRewindStart() {

    }

    @FXML
    public void onStepForward() {

    }

    @FXML
    public void onCanvasClicked() {

    }
}
