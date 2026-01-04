package app.presenter;

import app.model.*;
import app.shared.Config;
import app.simulation.AnimalInfo;
import app.simulation.Simulation;
import app.simulation.SimulationState;
import app.simulation.Statistics;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimulationController implements MapChangeListener {
    private static final String STAT_ANIMALS = "Liczba zwierząt";
    private static final String STAT_PLANTS = "Liczba roślin";
    private static final String STAT_FREE_FIELDS = "Wolne pola";
    private static final String STAT_AVG_ENERGY = "Średnia energia";
    private static final String STAT_AVG_LIFESPAN = "Średnia dł. życia";
    private static final String STAT_AVG_CHILDREN = "Średnia liczba dzieci";

    private static final Color COLOR_STEPPE = Color.LIMEGREEN;
    private static final Color COLOR_JUNGLE = Color.rgb(34, 139, 34); // ForestGreen
    private static final Color COLOR_PLANT = Color.DARKGREEN;
    private static final double JUNGLE_RATIO = 0.2;
    private static final double ANIMAL_HUE = 200.0;
    private static final double MIN_BRIGHTNESS = 0.4;
    private static final double BRIGHTNESS_RANGE = 0.6;

    @FXML private VBox animalStatsBox;
    @FXML private VBox genotypeListContainer;
    @FXML private HBox legendContainer;
    @FXML private Label dayLabel, allAnimalsLabel,
            allPlantsLabel, freeFieldsLabel, avgEnergyLabel,
            avgLifespanLabel, avgChildrenCount;
    @FXML private Label statusLabel;
    @FXML private Label noAnimalSelectedLabel;
    @FXML private Label trackedIdLabel, trackedActiveGeneLabel,
            trackedEnergyLabel, trackedPlantsLabel, trackedChildrenLabel,
            trackedDescendantsLabel, trackedAgeLabel, trackedDeathLabel;
    @FXML private Canvas simulationCanvas;
    @FXML private CheckBox showGridBox;
    @FXML private Slider zoomSlider;
    @FXML private TextArea trackedGenotypeArea;
    @FXML private ComboBox<String> statSelectionBox;
    @FXML private LineChart<Number, Number> statChart;

    private XYChart.Series<Number, Number> chartSeries;

    private double cellWidth;
    private double cellHeight;
    private int currentDayIndex;
    private boolean isLive = true;
    private UUID trackedAnimalId;
    private Integer cachedDeathDate;
    private Simulation simulation;
    private Config config;

    // INITIALIZATION //

    @FXML
    public void initialize() {
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> onRedrawNeeded());

        setupLegend();

        // When visibility is false, hide it completely
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

        statSelectionBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshChart());

        chartSeries = new XYChart.Series<>();
        statChart.getData().add(chartSeries);

        statChart.setAnimated(false);
    }

    // PUBLIC API //

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
            // Decide whether to update view to latest day
            // currentDayIndex is 1 indexed, but history is 0 indexed, hence -2
            if (isLive || currentDayIndex == simulation.getHistory().size() - 2) {
                List<SimulationState> history = simulation.getHistory();

                if (!history.isEmpty()) {
                    currentDayIndex = history.size() - 1;

                    onRedrawNeeded();
                }
            }
        });
    }

    // FXML HANDLERS //

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
        }
        else {
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

    @FXML
    private void onRedrawNeeded() {
        if (simulation == null || config == null) return;

        List<SimulationState> history = simulation.getHistory();
        if (history.isEmpty()) return;

        recalculateCanvasSize();

        if (currentDayIndex >= 0 && currentDayIndex < history.size()) {
            renderMap(history.get(currentDayIndex));
            updateStats(history.getLast().statistics());
        }
    }

    @FXML
    private void onStopTrackingClicked() {
        this.trackedAnimalId = null;
        animalStatsBox.setVisible(false);
        noAnimalSelectedLabel.setVisible(true);
        onRedrawNeeded();
    }

    // MAIN UI LOGIC //

    private void renderMap(SimulationState state) {
        GraphicsContext gc = simulationCanvas.getGraphicsContext2D();

        if (!recalculateCanvasSize()) return;
        double width = simulationCanvas.getWidth();
        double height = simulationCanvas.getHeight();

        gc.clearRect(0, 0, width, height);

        drawBackground(gc);
        if (showGridBox.isSelected()) {
            drawGrid(gc);
        }
        drawPlants(gc, state.plantPositions());
        drawAnimals(gc, state);

        if (trackedAnimalId != null && !isAnimalVisibleOnMap(state)) {
            handleDeadAnimal();
        }
    }

    private void updateStats(Statistics statistics) {
        updateGeneralStatisticsLabels(statistics);
        updateGenotypeLeaderboard(statistics);
        refreshChart();
    }

    // HELPER METHODS //

    // Render map helpers

    private boolean recalculateCanvasSize() {
        if (config == null || config.mapWidth() <= 0) return false;

        if (this.cellWidth <= 0 || zoomSlider.isValueChanging() || simulationCanvas.getWidth() != zoomSlider.getValue()) {
            double widthInPixels = zoomSlider.getValue();
            this.cellWidth = widthInPixels / config.mapWidth();
            this.cellHeight = this.cellWidth;
            double heightInPixels = this.cellWidth * config.mapHeight();

            simulationCanvas.setWidth(widthInPixels);
            simulationCanvas.setHeight(heightInPixels);
        }
        return true;
    }

    private void drawBackground(GraphicsContext gc) {
        double width = simulationCanvas.getWidth();
        double height = simulationCanvas.getHeight();

        gc.setFill(COLOR_STEPPE);
        gc.fillRect(0, 0, width, height);

        double jungleHeightPx = height * JUNGLE_RATIO;
        double jungleStartYPx = (height - jungleHeightPx) / 2;

        gc.setFill(COLOR_JUNGLE);
        gc.fillRect(0, jungleStartYPx, width, jungleHeightPx);
    }

    private void drawGrid(GraphicsContext gc) {
        double width = simulationCanvas.getWidth();
        double height = simulationCanvas.getHeight();

        if (showGridBox.isSelected()) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.15));
            gc.setLineWidth(1);

            for (double i = 0; i <= width; i += cellWidth) gc.strokeLine(i, 0, i, height);
            for (double i = 0; i <= height; i += cellHeight) gc.strokeLine(0, i, width, i);
        }
    }

    private void drawPlants(GraphicsContext gc, Map<Vector2d, Plant> plantPositions) {
        gc.setFill(COLOR_PLANT);
        for (Vector2d plantPos : plantPositions.keySet()) {
            gc.fillRect(plantPos.getX() * cellWidth, plantPos.getY() * cellHeight, cellWidth, cellHeight);
        }
    }

    private void drawAnimals(GraphicsContext gc, SimulationState state) {
        double currentMaxEnergy = Math.max(1.0, state.statistics().maxEnergy());
        String dominantGenotype = getDominantGenotype(state);

        for (List<AnimalInfo> animalsOnTile : state.animalPositions().values()) {
            for (AnimalInfo animalInfo : animalsOnTile) {
                drawSingleAnimal(gc, animalInfo, currentMaxEnergy, dominantGenotype);
            }
        }
    }

    private void drawSingleAnimal(GraphicsContext gc, AnimalInfo animalInfo, double maxEnergy, String dominantGenotype) {
        int x = animalInfo.position().getX();
        int y = animalInfo.position().getY();

        double energyRatio = Math.min(1.0, Math.max(0.0, (double) animalInfo.energy() / maxEnergy));
        double brightness = MIN_BRIGHTNESS + (BRIGHTNESS_RANGE * energyRatio);

        gc.setFill(Color.hsb(ANIMAL_HUE, 1.0, brightness));
        gc.fillOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

        if (animalInfo.genotype().equals(dominantGenotype)) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
        }

        if (animalInfo.id().equals(trackedAnimalId)) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

            updateTrackedAnimalPanel(animalInfo);
        }
    }

    private String getDominantGenotype(SimulationState state) {
        if (!state.statistics().topGenotypes().isEmpty()) {
            return state.statistics().topGenotypes().getFirst().getKey();
        }
        return null;
    }

    private boolean isAnimalVisibleOnMap(SimulationState state) {
        if (trackedAnimalId == null) return false;
        for (List<AnimalInfo> animals : state.animalPositions().values()) {
            for (AnimalInfo a : animals) {
                if (a.id().equals(trackedAnimalId)) return true;
            }
        }
        return false;
    }

    private void handleDeadAnimal() {
        if (cachedDeathDate == null) {
            cachedDeathDate = findDeathDate();
        }

        if (cachedDeathDate != null) {
            trackedDeathLabel.setText("Zmarł w dniu: " + cachedDeathDate);
            trackedEnergyLabel.setText("0");
            trackedActiveGeneLabel.setText("Aktywny gen: -");
        } else {
            trackedDeathLabel.setText("Nie ma na mapie");
            trackedEnergyLabel.setText("-");
        }
    }

    // UI Logic

    private void startTracking(UUID animalId) {
        this.cachedDeathDate = null;
        this.trackedAnimalId = animalId;

        animalStatsBox.setVisible(true);
        noAnimalSelectedLabel.setVisible(false);

        onRedrawNeeded();
    }

    private void updateTrackedAnimalPanel(AnimalInfo info) {
        trackedIdLabel.setText("ID" + info.id());
        trackedGenotypeArea.setText(info.genotype());
        trackedActiveGeneLabel.setText("Aktywny gen: " + info.activeGene());
        trackedEnergyLabel.setText(String.valueOf(info.energy()));
        trackedPlantsLabel.setText("Zjedzone rośliny: " + info.plantsEaten());
        trackedChildrenLabel.setText(String.valueOf(info.childrenCount()));
        trackedDescendantsLabel.setText("Potomkowie: " + info.descendantsCount());
        trackedAgeLabel.setText(String.valueOf(info.daysLived()));
        trackedDeathLabel.setText("");
    }

    private Integer findDeathDate() {
        List<SimulationState> history = simulation.getHistory();

        for (int i = currentDayIndex - 1; i >= 0; i--) {
            SimulationState state = history.get(i);

            for (List<AnimalInfo> animals : state.animalPositions().values()) {
                for (AnimalInfo animal : animals) {
                    if (animal.id().equals(trackedAnimalId)) {
                        return i + 2;
                    }
                }
            }
        }

        return null;
    }

    // Statistics & charts

    private void updateGeneralStatisticsLabels(Statistics statistics) {
        dayLabel.setText("Dzień: %d".formatted(currentDayIndex + 1));
        allAnimalsLabel.setText("%d".formatted(statistics.animalsCount()));
        allPlantsLabel.setText("%d".formatted(statistics.plantsCount()));
        freeFieldsLabel.setText("%d".formatted(statistics.freeFields()));

        avgEnergyLabel.setText("Śr. energia: %.2f".formatted(statistics.averageEnergy()));
        avgLifespanLabel.setText("Śr. dł. życia: %.2f".formatted(statistics.averageLifespan()));
        avgChildrenCount.setText("Śr. il. dzieci: %.2f".formatted(statistics.averageChildrenCount()));
    }

    private void updateGenotypeLeaderboard(Statistics statistics) {
        genotypeListContainer.getChildren().clear();

        if (statistics.topGenotypes().isEmpty()) {
            genotypeListContainer.getChildren().add(new Label("Brak danych"));
            return;
        }

        int rank = 1;
        for (Map.Entry<String, Integer> entry : statistics.topGenotypes()) {
            Label label = createGenotypeLabel(entry.getKey(), entry.getValue(), rank);
            genotypeListContainer.getChildren().add(label);
            rank++;
        }
    }

    private Label createGenotypeLabel(String genotype, int count, int rank) {
        String displayText = formatGenotypeText(genotype, count, rank);

        Label label = new Label(displayText);
        label.getStyleClass().add("genotype-item");

        if (rank == 1) {
            label.getStyleClass().add("genotype-leader");
        }

        return label;
    }

    private String formatGenotypeText(String genotype, int count, int rank) {
        if (genotype.length() > 20) {
            return String.format("%d. %s ... %s (%d)",
                    rank,
                    genotype.substring(0, 20),
                    genotype.substring(genotype.length() - 15),
                    count
            );
        } else {
            return String.format("%d. %s (%d)", rank, genotype, count);
        }
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

    // Initialize

    private void setupLegend() {
        legendContainer.getChildren().clear();

        legendContainer.getChildren().add(createLegendItem(COLOR_STEPPE, "Step"));
        legendContainer.getChildren().add(createLegendItem(COLOR_JUNGLE, "Dżungla"));
        legendContainer.getChildren().add(createLegendItem(COLOR_PLANT, "Roślina"));

        legendContainer.getChildren().add(new Separator(Orientation.VERTICAL));

        Color weakAnimalColor = Color.hsb(ANIMAL_HUE, 1.0, 0.5);
        legendContainer.getChildren().add(createLegendItem(weakAnimalColor, "Min Energia"));

        Color strongAnimalColor = Color.hsb(ANIMAL_HUE, 1.0, 1.0);
        legendContainer.getChildren().add(createLegendItem(strongAnimalColor, "Max Energia"));

        legendContainer.getChildren().add(new Separator(Orientation.VERTICAL));

        Color trackedAnimalColor = Color.WHITE;
        legendContainer.getChildren().add(createLegendItem(trackedAnimalColor, "Śledzony zwierzak"));

        Color dominantAnimalColor = Color.GOLD;
        legendContainer.getChildren().add(createLegendItem(dominantAnimalColor, "Dominujący genotyp"));
    }

    private HBox createLegendItem(Color color, String labelText) {
        HBox item = new HBox(5);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle icon = new Circle(7);
        icon.setFill(color);
        icon.setStroke(Color.GRAY);
        icon.setStrokeWidth(0.5);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px;");

        item.getChildren().addAll(icon, label);
        return item;
    }

}
