package app.presenter;

import app.model.*;
import app.shared.Config;
import app.simulation.AnimalInfo;
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

    private int currentDayIndex = 0;
    private boolean isLive = true;

    private Simulation simulation;
    private WorldMap map;
    private Config config;

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.map = simulation.getMap();

        simulation.addListener(this);

        if (config != null) {
            List<SimulationState> history = simulation.getHistory();

            if (!history.isEmpty()) {
                this.currentDayIndex = history.size() - 1;

                SimulationState initialState = history.get(currentDayIndex);

                drawSystem(initialState);

                 updateStats(initialState.statistics());
            } else {
                GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
                gc.setFill(Color.LIMEGREEN);
                gc.fillRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
            }
        }
    }

    @FXML
    public void initialize() {
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentZoom = newValue.doubleValue();
            updateCanvasSize();
            onRedrawNeeded();
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
            if (isLive || currentDayIndex == simulation.getHistory().size() - 2) {
                List<SimulationState> history = simulation.getHistory();

                if (!history.isEmpty()) {
                    currentDayIndex = history.size() - 1;

                    SimulationState latestState = history.get(currentDayIndex);
                    updateStats(latestState.statistics());
                    drawSystem(latestState);
                }
            }
        });
    }

    @FXML
    private void onRedrawNeeded() {
        List<SimulationState> history = simulation.getHistory();
        if (!history.isEmpty() && currentDayIndex < history.size()) {
            SimulationState state = history.get(currentDayIndex);
            drawSystem(state);
            updateStats(state.statistics());
        }

    }

    private void drawSystem(SimulationState state) {
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

            for (double i = 0; i <= size; i += cellWidth) gc.strokeLine(i, 0, i, size);
            for (double i = 0; i <= size; i += cellHeight) gc.strokeLine(0, i, size, i);
        }

        for (Vector2d plantPos : state.plantPositions().keySet()) {
            gc.setFill(Color.FORESTGREEN);
            gc.fillRect(plantPos.getX() * cellWidth, plantPos.getY() * cellHeight, cellWidth, cellHeight);
        }

        for (List<AnimalInfo> animalsOnTile : state.animalPositions().values()) {
            for (AnimalInfo animalInfo : animalsOnTile) {

                int x = animalInfo.position().getX();
                int y = animalInfo.position().getY();
                int energy = animalInfo.energy();

                double energyRatio = Math.min(1.0, (double) energy / 100.0);
                gc.setFill(Color.hsb(energyRatio * 120.0, 1.0, 1.0));

                gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
            }
        }

        dayLabel.setText("Dzień: %s".formatted(currentDayIndex + 1));
    }

    private void updateStats(Statistics statistics) {
        allAnimalsLabel.setText("Zwierzaki: %s".formatted(statistics.animalsCount()));
        allPlantsLabel.setText("Rośliny: %s".formatted(statistics.plantsCount()));
        freeFieldsLabel.setText("Wolne pola: %s".formatted(statistics.freeFields()));
        avgEnergyLabel.setText("Śr. energia: %.2f".formatted(statistics.averageEnergy()));
        avgLifespanLabel.setText("Śr. dł. życia: %.2f".formatted(statistics.averageLifespan()));
        avgChildrenCount.setText("Śr. il. dzieci: %.2f".formatted(statistics.averageChildrenCount()));
    }

    @FXML
    private void onPauseClicked() {
        if(simulation != null) simulation.pause();
        isLive = false;
        statusLabel.setText("Symulacja zatrzymana");
    }

    @FXML
    private void onResumeClicked() {
        if(simulation != null) simulation.resume();
        isLive = true;
        statusLabel.setText("Symulacja trwa...");
    }

    @FXML
    public void onRewindToStart() {
        onPauseClicked();

        currentDayIndex = 0;
        onRedrawNeeded();
    }

    @FXML
    public void onStepBack() {
        onPauseClicked();

        if (currentDayIndex > 0) {
            currentDayIndex--;
            onRedrawNeeded();
        }
    }

    @FXML
    public void onStepForward() {
        onPauseClicked();

        List<SimulationState> history = simulation.getHistory();

        if (currentDayIndex < history.size() - 1) {
            currentDayIndex++;
            SimulationState state = history.get(currentDayIndex);
            drawSystem(state);
        }
        else {
            simulation.step();
            List<SimulationState> newHistory = simulation.getHistory();
            drawSystem(newHistory.getLast());
        }
    }

    @FXML
    public void onStepEnd() {
        onPauseClicked();
        List<SimulationState> history = simulation.getHistory();

        currentDayIndex = history.size() - 1;

        onRedrawNeeded();
    }

    @FXML
    public void onCanvasClicked() {

    }
}
