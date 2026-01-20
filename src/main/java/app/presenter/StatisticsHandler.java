package app.presenter;

import app.simulation.Statistics;
import app.simulation.SimulationState;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsHandler {
    private static final String STAT_ANIMALS = "Liczba zwierząt";
    private static final String STAT_PLANTS = "Liczba roślin";
    private static final String STAT_FREE_FIELDS = "Wolne pola";
    private static final String STAT_AVG_ENERGY = "Średnia energia";
    private static final String STAT_AVG_LIFESPAN = "Średnia dł. życia";
    private static final String STAT_AVG_CHILDREN = "Średnia liczba dzieci";

    private final LineChart<Number, Number> statChart;
    private final ComboBox<String> statSelectionBox;
    private final VBox genotypeListContainer;

    private final Label dayLabel;
    private final Label allAnimalsLabel;
    private final Label allPlantsLabel;
    private final Label freeFieldsLabel;
    private final Label avgEnergyLabel;
    private final Label avgLifespanLabel;
    private final Label avgChildrenCount;

    private XYChart.Series<Number, Number> chartSeries;
    private List<SimulationState> cachedHistory;
    private int cachedDayIndex;

    public StatisticsHandler(LineChart<Number, Number> statChart,
                             ComboBox<String> statSelectionBox,
                             VBox genotypeListContainer,
                             Label dayLabel,
                             Label allAnimalsLabel,
                             Label allPlantsLabel,
                             Label freeFieldsLabel,
                             Label avgEnergyLabel,
                             Label avgLifespanLabel,
                             Label avgChildrenCount) {
        this.statChart = statChart;
        this.statSelectionBox = statSelectionBox;
        this.genotypeListContainer = genotypeListContainer;
        this.dayLabel = dayLabel;
        this.allAnimalsLabel = allAnimalsLabel;
        this.allPlantsLabel = allPlantsLabel;
        this.freeFieldsLabel = freeFieldsLabel;
        this.avgEnergyLabel = avgEnergyLabel;
        this.avgLifespanLabel = avgLifespanLabel;
        this.avgChildrenCount = avgChildrenCount;

        initializeComponents();
    }

    public void update(SimulationState currentState, List<SimulationState> history, int currentDayIndex) {
        if (currentState == null) return;

        this.cachedHistory = history;
        this.cachedDayIndex = currentDayIndex;

        updateGeneralLabels(currentState.statistics(), currentDayIndex);
        updateGenotypeLeaderboard(currentState.statistics());
        refreshChart();
    }

    private void initializeComponents() {
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

    private void updateGeneralLabels(Statistics stats, int currentDayIndex) {
        dayLabel.setText("Dzień: %d".formatted(currentDayIndex + 1));
        allAnimalsLabel.setText("%d".formatted(stats.animalsCount()));
        allPlantsLabel.setText("%d".formatted(stats.plantsCount()));
        freeFieldsLabel.setText("%d".formatted(stats.freeFields()));

        avgEnergyLabel.setText("Śr. energia: %.2f".formatted(stats.averageEnergy()));
        avgLifespanLabel.setText("Śr. dł. życia: %.2f".formatted(stats.averageLifespan()));
        avgChildrenCount.setText("Śr. il. dzieci: %.2f".formatted(stats.averageChildrenCount()));
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
        if (cachedHistory == null || cachedHistory.isEmpty()) return;

        String selectedStat = statSelectionBox.getValue();
        if (selectedStat == null) return;

        int endIndex = Math.min(cachedDayIndex, cachedHistory.size() - 1);
        int totalDataPoints = endIndex + 1;

        int step = 1;
        if (totalDataPoints > 10000) step = 10;
        else if (totalDataPoints > 5000) step = 5;
        else if (totalDataPoints > 2000) step = 2;

        boolean isSameStat = chartSeries.getName() != null && chartSeries.getName().equals(selectedStat);
        boolean isNoDownsampling = (step == 1);
        boolean isNextDay = (chartSeries.getData().size() == endIndex);

        if (isSameStat && isNoDownsampling && isNextDay) {
            SimulationState currentState = cachedHistory.get(endIndex);
            double value = getStatValue(currentState.statistics(), selectedStat);
            chartSeries.getData().add(new XYChart.Data<>(endIndex + 1, value));
        } else {
            chartSeries.setName(selectedStat);
            ArrayList<XYChart.Data<Number, Number>> dataBuffer = new ArrayList<>();

            for (int i = 0; i <= endIndex; i += step) {
                SimulationState state = cachedHistory.get(i);
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
}