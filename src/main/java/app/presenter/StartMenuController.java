package app.presenter;

import app.shared.Config;
import app.simulation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class StartMenuController {
    private static final String CONFIG_DIR = "configs";

    @FXML private ComboBox<ConfigPreset> presetComboBox;
    @FXML private TextField configNameField;
    @FXML private Label configInfo, errorLabel;

    @FXML private Spinner<Integer> mapWidthSpinner, mapHeightSpinner;
    @FXML private Spinner<Integer> initialPlantsNumSpinner, plantMinEnergySpinner, plantMaxEnergySpinner, dailyPlantGrowthSpinner;
    @FXML private Spinner<Integer> initialAnimalsNumSpinner, animalEnergyLossSpinner, animalStartEnergySpinner;
    @FXML private Spinner<Integer> energyNeededToReproduceSpinner, energyUsedToReproduceSpinner;
    @FXML private Spinner<Integer> minMutationsSpinner, maxMutationsSpinner, genomeLengthSpinner;

    @FXML private CheckBox isFomoCheckBox;
    @FXML private Spinner<Integer> fomoRadiusSpinner, fomoCrowdSizeSpinner;

    private final SimulationEngine simulationEngine = new SimulationEngine();
    private ConfigurationManager configManager;
    private PresetManager presetManager;

    @FXML
    public void initialize() {
        this.configManager = new ConfigurationManager(CONFIG_DIR);
        this.presetManager = new PresetManager(CONFIG_DIR, configManager);

        loadPresets();
        setupValidation();

        presetComboBox.setOnAction(event -> loadSelectedPreset());
    }

    private void setupValidation() {
        FxUtils.configureSpinnerCommit(
                mapWidthSpinner, mapHeightSpinner, initialPlantsNumSpinner,
                plantMinEnergySpinner, plantMaxEnergySpinner, dailyPlantGrowthSpinner,
                initialAnimalsNumSpinner, animalEnergyLossSpinner, animalStartEnergySpinner,
                energyNeededToReproduceSpinner, energyUsedToReproduceSpinner,
                minMutationsSpinner, maxMutationsSpinner, genomeLengthSpinner,
                fomoRadiusSpinner, fomoCrowdSizeSpinner
        );

        FxUtils.setupRangeValidation(minMutationsSpinner, maxMutationsSpinner);
        FxUtils.setupRangeValidation(plantMinEnergySpinner, plantMaxEnergySpinner);
    }

    @FXML
    private void onStartClicked() {
        try {
            Config config = getConfigFromForm();
            startSimulationWindow(config);
            errorLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Błąd: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveConfigClicked() {
        String name = configNameField.getText().trim();
        if (name.isEmpty()) {
            showInfo("Podaj nazwę konfiguracji!", true);
            return;
        }

        try {
            configManager.saveConfig(getConfigFromForm(), name);
            loadPresets();
            showInfo("Zapisano konfigurację: " + name, false);
        } catch (Exception e) {
            errorLabel.setText("Błąd zapisu: " + e.getMessage());
        }
    }

    private void loadPresets() {
        List<ConfigPreset> presets = presetManager.loadAllPresets();
        presetComboBox.getItems().setAll(presets);
    }

    private void loadSelectedPreset() {
        ConfigPreset selected = presetComboBox.getValue();
        if (selected != null) {
            updateGuiFromConfig(selected.config());
            showInfo("Wczytano preset: " + selected, false);
        }
    }

    private void showInfo(String message, boolean isError) {
        configInfo.setText(message);
        configInfo.setTextFill(isError ? Color.RED : Color.GREEN);
    }

    private void startSimulationWindow(Config config) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SimulationView.fxml"));
        BorderPane root = loader.load();

        SimulationController controller = loader.getController();
        Simulation simulation = new Simulation(config);

        controller.setConfig(config);
        controller.setSimulation(simulation);

        Stage stage = new Stage();
        stage.setTitle("Symulacja ID: " + simulation.getId());
        stage.setScene(new Scene(root));
        stage.show();

        simulationEngine.runAsync(simulation);

        stage.setOnCloseRequest(e -> simulation.end());
    }

    private Config getConfigFromForm() {
        return new Config(
                mapWidthSpinner.getValue(), mapHeightSpinner.getValue(),
                initialPlantsNumSpinner.getValue(), plantMinEnergySpinner.getValue(),
                plantMaxEnergySpinner.getValue(), dailyPlantGrowthSpinner.getValue(),
                initialAnimalsNumSpinner.getValue(), animalEnergyLossSpinner.getValue(),
                animalStartEnergySpinner.getValue(), energyNeededToReproduceSpinner.getValue(),
                energyUsedToReproduceSpinner.getValue(), minMutationsSpinner.getValue(),
                maxMutationsSpinner.getValue(), genomeLengthSpinner.getValue(),
                isFomoCheckBox.isSelected(), fomoRadiusSpinner.getValue(), fomoCrowdSizeSpinner.getValue()
        );
    }

    private void updateGuiFromConfig(Config config) {
        mapWidthSpinner.getValueFactory().setValue(config.mapWidth());
        mapHeightSpinner.getValueFactory().setValue(config.mapHeight());
        initialPlantsNumSpinner.getValueFactory().setValue(config.initialPlantsNum());
        plantMinEnergySpinner.getValueFactory().setValue(config.plantMinEnergy());
        plantMaxEnergySpinner.getValueFactory().setValue(config.plantMaxEnergy());
        dailyPlantGrowthSpinner.getValueFactory().setValue(config.dailyPlantGrowth());
        initialAnimalsNumSpinner.getValueFactory().setValue(config.initialAnimalsNum());
        animalStartEnergySpinner.getValueFactory().setValue(config.animalStartEnergy());
        animalEnergyLossSpinner.getValueFactory().setValue(config.animalEnergyLoss());
        energyNeededToReproduceSpinner.getValueFactory().setValue(config.energyNeededToReproduce());
        energyUsedToReproduceSpinner.getValueFactory().setValue(config.energyUsedToReproduce());
        minMutationsSpinner.getValueFactory().setValue(config.minMutations());
        maxMutationsSpinner.getValueFactory().setValue(config.maxMutations());
        genomeLengthSpinner.getValueFactory().setValue(config.genomLength());
        isFomoCheckBox.setSelected(config.isFomo());
        fomoRadiusSpinner.getValueFactory().setValue(config.fomoRadius());
        fomoCrowdSizeSpinner.getValueFactory().setValue(config.fomoCrowdSize());
    }
}