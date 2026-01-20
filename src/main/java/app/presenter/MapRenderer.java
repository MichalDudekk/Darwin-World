package app.presenter;

import app.model.Plant;
import app.model.Vector2d;
import app.shared.Config;
import app.simulation.AnimalInfo;
import app.simulation.SimulationState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapRenderer {
    private static final Color COLOR_STEPPE = Color.LIMEGREEN;
    private static final Color COLOR_JUNGLE = Color.rgb(34, 139, 34); // ForestGreen
    private static final Color COLOR_PLANT = Color.DARKGREEN;
    private static final double JUNGLE_RATIO = 0.2;
    private static final double ANIMAL_HUE = 200.0;
    private static final double MIN_BRIGHTNESS = 0.4;
    private static final double BRIGHTNESS_RANGE = 0.6;

    private final Canvas canvas;
    private final GraphicsContext gc;

    private double cellWidth;
    private double cellHeight;

    public MapRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void render(SimulationState state, Config config, double zoomValue, boolean showGrid, UUID trackedAnimalId) {
        if (config == null || state == null) return;

        if (!recalculateCanvasSize(config, zoomValue)) return;

        clearCanvas();
        drawBackground();

        if (showGrid) {
            drawGrid();
        }

        drawPlants(state.plantPositions());
        drawAnimals(state, trackedAnimalId);
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    private boolean recalculateCanvasSize(Config config, double zoomValue) {
        if (config.mapWidth() <= 0) return false;

        if (this.cellWidth <= 0 || canvas.getWidth() != zoomValue) {
            double widthInPixels = zoomValue;
            this.cellWidth = widthInPixels / config.mapWidth();
            this.cellHeight = this.cellWidth;
            double heightInPixels = this.cellWidth * config.mapHeight();

            canvas.setWidth(widthInPixels);
            canvas.setHeight(heightInPixels);
        }
        return true;
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawBackground() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.setFill(COLOR_STEPPE);
        gc.fillRect(0, 0, width, height);

        double jungleHeightPx = height * JUNGLE_RATIO;
        double jungleStartYPx = (height - jungleHeightPx) / 2;

        gc.setFill(COLOR_JUNGLE);
        gc.fillRect(0, jungleStartYPx, width, jungleHeightPx);
    }

    private void drawGrid() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.setStroke(Color.rgb(0, 0, 0, 0.15));
        gc.setLineWidth(1);

        for (double i = 0; i <= width; i += cellWidth) gc.strokeLine(i, 0, i, height);
        for (double i = 0; i <= height; i += cellHeight) gc.strokeLine(0, i, width, i);
    }

    private void drawPlants(Map<Vector2d, Plant> plantPositions) {
        gc.setFill(COLOR_PLANT);
        for (Vector2d plantPos : plantPositions.keySet()) {
            gc.fillRect(plantPos.getX() * cellWidth, plantPos.getY() * cellHeight, cellWidth, cellHeight);
        }
    }

    private void drawAnimals(SimulationState state, UUID trackedAnimalId) {
        double currentMaxEnergy = Math.max(1.0, state.statistics().maxEnergy());
        String dominantGenotype = getDominantGenotype(state);

        for (List<AnimalInfo> animalsOnTile : state.animalPositions().values()) {
            for (AnimalInfo animalInfo : animalsOnTile) {
                drawSingleAnimal(animalInfo, currentMaxEnergy, dominantGenotype, trackedAnimalId);
            }
        }
    }

    private void drawSingleAnimal(AnimalInfo animalInfo, double maxEnergy, String dominantGenotype, UUID trackedAnimalId) {
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
        }
    }

    private String getDominantGenotype(SimulationState state) {
        if (!state.statistics().topGenotypes().isEmpty()) {
            return state.statistics().topGenotypes().getFirst().getKey();
        }
        return null;
    }
}