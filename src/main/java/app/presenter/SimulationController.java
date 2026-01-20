package app.presenter;

import app.model.MapChangeListener;
import app.model.Vector2d;
import app.model.WorldMap;
import app.shared.Config;
import app.simulation.AnimalInfo;
import app.simulation.Simulation;
import app.simulation.SimulationState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class SimulationController implements MapChangeListener {
    @FXML private VBox animalStatsBox;
    @FXML private VBox genotypeListContainer;
    @FXML private Label dayLabel, allAnimalsLabel, allPlantsLabel, freeFieldsLabel,
            avgEnergyLabel, avgLifespanLabel, avgChildrenCount;
    @FXML private ComboBox<String> statSelectionBox;
    @FXML private LineChart<Number, Number> statChart;

    @FXML private Label noAnimalSelectedLabel;
    @FXML private Label trackedIdLabel, trackedActiveGeneLabel, trackedEnergyLabel,
            trackedPlantsLabel, trackedChildrenLabel, trackedDescendantsLabel,
            trackedAgeLabel, trackedDeathLabel;
    @FXML private TextArea trackedGenotypeArea;

    @FXML private Canvas simulationCanvas;
    @FXML private HBox legendContainer;
    @FXML private Label statusLabel;
    @FXML private CheckBox showGridBox;
    @FXML private Slider zoomSlider;

    private MapRenderer mapRenderer;
    private StatisticsHandler statsHandler;
    private AnimalTracker animalTracker;

    private int currentDayIndex;
    private boolean isLive = true;
    private Simulation simulation;
    private Config config;

    @FXML
    public void initialize() {
        this.mapRenderer = new MapRenderer(simulationCanvas);

        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> onRedrawNeeded());

        animalStatsBox.managedProperty().bind(animalStatsBox.visibleProperty());
        noAnimalSelectedLabel.managedProperty().bind(noAnimalSelectedLabel.visibleProperty());

        this.statsHandler = new StatisticsHandler(
                statChart, statSelectionBox, genotypeListContainer,
                dayLabel, allAnimalsLabel, allPlantsLabel, freeFieldsLabel,
                avgEnergyLabel, avgLifespanLabel, avgChildrenCount
        );

        this.animalTracker = new AnimalTracker(
                animalStatsBox, noAnimalSelectedLabel, trackedIdLabel, trackedActiveGeneLabel,
                trackedEnergyLabel, trackedPlantsLabel, trackedChildrenLabel, trackedDescendantsLabel,
                trackedAgeLabel, trackedDeathLabel, trackedGenotypeArea
        );
        this.animalTracker.stopTracking();

        MapLegend mapLegend = new MapLegend();
        mapLegend.setupLegend(legendContainer);
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        simulation.addListener(this);

        if (config != null) {
            simulation.step();
        }
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
                    onRedrawNeeded();
                }
            }
        });
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
        } else {
            simulation.step();
        }
        onRedrawNeeded();
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

        if (mapRenderer.getCellWidth() <= 0) return;

        int mapX = (int) (clickX / mapRenderer.getCellWidth());
        int mapY = (int) (clickY / mapRenderer.getCellHeight());
        Vector2d clickedPosition = new Vector2d(mapX, mapY);

        SimulationState currentState = history.get(currentDayIndex);
        List<AnimalInfo> animalsOnTile = currentState.animalPositions().get(clickedPosition);

        if (animalsOnTile != null && !animalsOnTile.isEmpty()) {
            animalTracker.startTracking(animalsOnTile.getFirst().id());
        } else {
            animalTracker.stopTracking();
        }

        onRedrawNeeded();
    }

    @FXML
    private void onStopTrackingClicked() {
        animalTracker.stopTracking();
        onRedrawNeeded();
    }

    @FXML
    private void onRedrawNeeded() {
        if (simulation == null || config == null) return;
        List<SimulationState> history = simulation.getHistory();
        if (history.isEmpty()) return;

        if (currentDayIndex >= 0 && currentDayIndex < history.size()) {
            SimulationState currentState = history.get(currentDayIndex);

            mapRenderer.render(
                    currentState,
                    config,
                    zoomSlider.getValue(),
                    showGridBox.isSelected(),
                    animalTracker.getTrackedId()
            );

            statsHandler.update(currentState, history, currentDayIndex);

            animalTracker.update(currentState, history, currentDayIndex);
        }
    }
}