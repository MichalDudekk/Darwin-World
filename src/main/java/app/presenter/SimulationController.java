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
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.UUID;

public class SimulationController implements MapChangeListener {
    @FXML private Canvas simulationCanvas;
    @FXML private Label statusLabel;
    @FXML private CheckBox showGridBox;
    @FXML private Slider zoomSlider;
    @FXML private Label dayLabel, allAnimalsLabel, allPlantsLabel, freeFieldsLabel, avgEnergyLabel, avgLifespanLabel, avgChildrenCount;
    @FXML private VBox animalStatsBox;
    @FXML private Label noAnimalSelectedLabel;
    @FXML private Label trackedIdLabel, trackedActiveGeneLabel,
            trackedEnergyLabel, trackedPlantsLabel, trackedChildrenLabel,
            trackedDescendantsLabel, trackedAgeLabel, trackedDeathLabel;
    @FXML private TextArea trackedGenotypeArea;

    private double currentZoom = 600.0;

    private double cellWidth;
    private double cellHeight;

    private int currentDayIndex = 0;
    private boolean isLive = true;
    private UUID trackedAnimalId = null;

    private Simulation simulation;
    private WorldMap map;
    private Config config;

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.map = simulation.getMap();

        simulation.addListener(this);

        if (config != null) {
            simulation.step();
        }
    }

    @FXML
    public void initialize() {
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentZoom = newValue.doubleValue();
            onRedrawNeeded();
        });
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
        if (simulation == null || config == null) return;

        List<SimulationState> history = simulation.getHistory();
        if (history.isEmpty()) return;

        double widthInPixels = zoomSlider.getValue();

        if (config.mapWidth() <= 0) {
            System.err.println("BŁĄD: Szerokość mapy w configu to 0! Ustawiam domyślne 1.");
            return;
        }

        double cellSize = widthInPixels / config.mapWidth();
        double heightInPixels = cellSize * config.mapHeight();

        simulationCanvas.setWidth(widthInPixels);
        simulationCanvas.setHeight(heightInPixels);

        if (Double.isInfinite(heightInPixels) || Double.isNaN(heightInPixels)) {
            System.err.println("BŁĄD: Wysokość Canvasu wyszła nieskończona!");
            return;
        }

        this.cellWidth = cellSize;
        this.cellHeight = cellSize;

        if (currentDayIndex >= 0 && currentDayIndex < history.size()) {
            drawSystem(history.get(currentDayIndex));
        }
    }

    private void drawSystem(SimulationState state) {
        GraphicsContext gc = simulationCanvas.getGraphicsContext2D();

        double width = simulationCanvas.getWidth();
        double height = simulationCanvas.getHeight();

        if (this.cellWidth <= 0) {
            if (config != null && config.mapWidth() > 0) {
                this.cellWidth = width / config.mapWidth();
                this.cellHeight = this.cellWidth;

                height = this.cellWidth * config.mapHeight();
                simulationCanvas.setHeight(height);
            } else {
                return;
            }
        }

        // Background fill
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(0, 0, width, height);

        if (showGridBox.isSelected()) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.15));
            gc.setLineWidth(1);

            for (double i = 0; i <= width; i += cellWidth) gc.strokeLine(i, 0, i, height);
            for (double i = 0; i <= height; i += cellHeight) gc.strokeLine(0, i, width, i);
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

                if (trackedAnimalId != null && animalInfo.id() == trackedAnimalId) {
                    gc.setStroke(Color.WHITE); // lub BLUE
                    gc.setLineWidth(2);
                    gc.strokeOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                    updateTrackedAnimalPanel(animalInfo);
                }
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
    private void onCanvasClicked(MouseEvent event) {
        if (simulation == null) return;
        List<SimulationState> history = simulation.getHistory();
        if (history.isEmpty()) return;

        double clickX = event.getX();
        double clickY = event.getY();

        int mapX = (int) (clickX / cellWidth);
        int mapY = (int) (clickY / cellHeight);
        Vector2d clickedPosition = new Vector2d(mapX, mapY);

        SimulationState currentState = history.get(currentDayIndex);
        List<AnimalInfo> animalsOnTile = currentState.animalPositions().get(clickedPosition);

        if (animalsOnTile != null && !animalsOnTile.isEmpty()) {
            AnimalInfo selected = animalsOnTile.getFirst();
            startTracking(selected.id());
        } else {
             onStopTrackingClicked();
        }
    }

    private void startTracking(UUID animalId) {
        this.trackedAnimalId = animalId;
        animalStatsBox.setVisible(true);
        noAnimalSelectedLabel.setVisible(false);

        onRedrawNeeded();
    }

    @FXML
    private void onStopTrackingClicked() {
        this.trackedAnimalId = null;
        animalStatsBox.setVisible(false);
        noAnimalSelectedLabel.setVisible(true);
        onRedrawNeeded();
    }

    private void updateTrackedAnimalPanel(AnimalInfo info) {
        trackedIdLabel.setText("ID: " + info.id());
        trackedGenotypeArea.setText("Genom: " + info.genotype());
        trackedActiveGeneLabel.setText("Aktywny gen: " + info.activeGene());
        trackedEnergyLabel.setText("Energia: " + info.energy());
        trackedPlantsLabel.setText("Zjedzone rośliny: " + info.plantsEaten());
        trackedChildrenLabel.setText("Dzieci: " + info.childrenCount());
        trackedDescendantsLabel.setText("Potomkowie: " + info.descendantsCount());
        trackedAgeLabel.setText("Wiek: " + info.daysLived());

        if (info.deathDay() != null) {
            trackedDeathLabel.setText("Zmarł w dniu: " + info.deathDay());
        } else {
            trackedDeathLabel.setText("");
        }
    }
}
