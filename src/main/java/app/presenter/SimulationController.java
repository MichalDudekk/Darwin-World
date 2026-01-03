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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimulationController implements MapChangeListener {
    private static final String STAT_ANIMALS = "Liczba zwierząt";
    private static final String STAT_PLANTS = "Liczba roślin";
    private static final String STAT_FREE_FIELDS = "Wolne pola";
    private static final String STAT_AVG_ENERGY = "Średnia energia";
    private static final String STAT_AVG_LIFESPAN = "Średnia dł. życia";
    private static final String STAT_AVG_CHILDREN = "Średnia liczba dzieci";

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
    @FXML private ComboBox<String> statSelectionBox;
    @FXML private LineChart<Number, Number> statChart;
    private XYChart.Series<Number, Number> chartSeries;

    private double currentZoom = 600.0;

    private double cellWidth;
    private double cellHeight;

    private int currentDayIndex = 0;
    private boolean isLive = true;
    private UUID trackedAnimalId = null;

    private Simulation simulation;
    private WorldMap map;
    private Config config;

    private XYChart.Series<Number, Number> animalSeries;
    private XYChart.Series<Number, Number> plantSeries;

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

        animalStatsBox.managedProperty().bind(animalStatsBox.visibleProperty());
        noAnimalSelectedLabel.managedProperty().bind(noAnimalSelectedLabel.visibleProperty());

        statSelectionBox.getItems().addAll(
                STAT_ANIMALS,
                STAT_PLANTS,
                STAT_FREE_FIELDS,
                STAT_AVG_ENERGY,
                STAT_AVG_LIFESPAN,
                STAT_AVG_CHILDREN
        );

        statSelectionBox.getSelectionModel().select(STAT_ANIMALS);

        statSelectionBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            refreshChart();
        });

        chartSeries = new XYChart.Series<>();
        statChart.getData().add(chartSeries);

        statChart.setAnimated(false);
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

        refreshChart();
    }

    private void refreshChart() {
        if (simulation == null) return;
        List<SimulationState> history = simulation.getHistory();
        if (history.isEmpty()) return;

        String selectedStat = statSelectionBox.getValue();
        if (selectedStat == null) return;

        int endIndex = Math.min(currentDayIndex, history.size() - 1);
        int totalDataPoints = endIndex + 1;

        int step = 1;
        if (totalDataPoints > 10000) step = 10;
        else if (totalDataPoints > 5000) step = 5;
        else if (totalDataPoints > 2000) step = 2;

        boolean isSameStat = chartSeries.getName() != null && chartSeries.getName().equals(selectedStat);
        boolean isNoDownsampling = (step == 1);
        boolean isNextDay = (chartSeries.getData().size() == endIndex);

        if (isSameStat && isNoDownsampling && isNextDay) {
            SimulationState currentState = history.get(endIndex);
            double value = getStatValue(currentState.statistics(), selectedStat);
            chartSeries.getData().add(new XYChart.Data<>(endIndex + 1, value));

        } else {
            chartSeries.setName(selectedStat);
            ArrayList<XYChart.Data<Number, Number>> dataBuffer = new ArrayList<>();

            for (int i = 0; i <= endIndex; i += step) {
                SimulationState state = history.get(i);
                double value = getStatValue(state.statistics(), selectedStat);

                dataBuffer.add(new XYChart.Data<>(i + 1, value));
            }

            chartSeries.getData().setAll(dataBuffer);
        }
    }

    private double getStatValue(Statistics stats, String selectedStat) {
        return switch (selectedStat) {
            case STAT_ANIMALS -> stats.animalsCount();
            case STAT_PLANTS -> stats.plantsCount();
            case STAT_FREE_FIELDS -> stats.freeFields();
            case STAT_AVG_ENERGY -> stats.averageEnergy();
            case STAT_AVG_LIFESPAN -> stats.averageLifespan();
            case STAT_AVG_CHILDREN -> stats.averageChildrenCount();
            default -> 0.0;
        };
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
        SimulationState state = simulation.getHistory().get(currentDayIndex);
        updateStats(state.statistics());
        onRedrawNeeded();
    }

    @FXML
    public void onStepBack() {
        onPauseClicked();

        if (currentDayIndex > 0) {
            currentDayIndex--;

            SimulationState state = simulation.getHistory().get(currentDayIndex);
            updateStats(state.statistics());
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

        SimulationState state = simulation.getHistory().getLast();
        updateStats(state.statistics());
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
