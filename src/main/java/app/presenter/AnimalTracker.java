package app.presenter;

import app.simulation.AnimalInfo;
import app.simulation.SimulationState;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

public class AnimalTracker {
    private final VBox animalStatsBox;
    private final Label noAnimalSelectedLabel;
    private final Label trackedIdLabel;
    private final Label trackedActiveGeneLabel;
    private final Label trackedEnergyLabel;
    private final Label trackedPlantsLabel;
    private final Label trackedChildrenLabel;
    private final Label trackedDescendantsLabel;
    private final Label trackedAgeLabel;
    private final Label trackedDeathLabel;
    private final TextArea trackedGenotypeArea;

    private UUID trackedAnimalId;
    private Integer cachedDeathDate;

    public AnimalTracker(VBox animalStatsBox,
                         Label noAnimalSelectedLabel,
                         Label trackedIdLabel,
                         Label trackedActiveGeneLabel,
                         Label trackedEnergyLabel,
                         Label trackedPlantsLabel,
                         Label trackedChildrenLabel,
                         Label trackedDescendantsLabel,
                         Label trackedAgeLabel,
                         Label trackedDeathLabel,
                         TextArea trackedGenotypeArea) {
        this.animalStatsBox = animalStatsBox;
        this.noAnimalSelectedLabel = noAnimalSelectedLabel;
        this.trackedIdLabel = trackedIdLabel;
        this.trackedActiveGeneLabel = trackedActiveGeneLabel;
        this.trackedEnergyLabel = trackedEnergyLabel;
        this.trackedPlantsLabel = trackedPlantsLabel;
        this.trackedChildrenLabel = trackedChildrenLabel;
        this.trackedDescendantsLabel = trackedDescendantsLabel;
        this.trackedAgeLabel = trackedAgeLabel;
        this.trackedDeathLabel = trackedDeathLabel;
        this.trackedGenotypeArea = trackedGenotypeArea;
    }


    public void startTracking(UUID animalId) {
        this.trackedAnimalId = animalId;
        this.cachedDeathDate = null;

        animalStatsBox.setVisible(true);
        noAnimalSelectedLabel.setVisible(false);
    }

    public void stopTracking() {
        this.trackedAnimalId = null;
        this.cachedDeathDate = null;

        animalStatsBox.setVisible(false);
        noAnimalSelectedLabel.setVisible(true);
    }

    public void update(SimulationState currentState, List<SimulationState> history, int currentDayIndex) {
        if (trackedAnimalId == null) return;

        AnimalInfo currentInfo = findAnimalInState(currentState);

        if (currentInfo != null) {
            updatePanelAlive(currentInfo);
            cachedDeathDate = null;
        } else {
            handleDeadAnimal(history, currentDayIndex);
        }
    }

    public UUID getTrackedId() {
        return trackedAnimalId;
    }

    private AnimalInfo findAnimalInState(SimulationState state) {
        for (List<AnimalInfo> animals : state.animalPositions().values()) {
            for (AnimalInfo animal : animals) {
                if (animal.id().equals(trackedAnimalId)) {
                    return animal;
                }
            }
        }
        return null;
    }

    private void updatePanelAlive(AnimalInfo info) {
        trackedIdLabel.setText("ID: " + info.id());
        trackedGenotypeArea.setText(info.genotype());
        trackedActiveGeneLabel.setText("Aktywny gen: " + info.activeGene());
        trackedEnergyLabel.setText(String.valueOf(info.energy()));
        trackedPlantsLabel.setText("Zjedzone rośliny: " + info.plantsEaten());
        trackedChildrenLabel.setText(String.valueOf(info.childrenCount()));
        trackedDescendantsLabel.setText("Potomkowie: " + info.descendantsCount());
        trackedAgeLabel.setText(String.valueOf(info.daysLived()));
        trackedDeathLabel.setText("");
    }

    private void handleDeadAnimal(List<SimulationState> history, int currentDayIndex) {
        if (cachedDeathDate == null) {
            cachedDeathDate = findDeathDate(history, currentDayIndex);
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

    private Integer findDeathDate(List<SimulationState> history, int currentDayIndex) {
        for (int i = currentDayIndex - 1; i >= 0; i--) {
            SimulationState state = history.get(i);
            if (findAnimalInState(state) != null) {
                // Skoro w dniu 'i' był, a w 'currentDayIndex' go nie ma,
                // to znaczy, że umarł w dniu i+2
                return i + 2;
            }
        }
        return null;
    }
}