package app.presenter;

import app.shared.Config;
import app.simulation.ConfigPreset;
import app.simulation.ConfigurationManager;
import app.simulation.Simulation;
import app.simulation.SimulationEngine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartMenuController {
    private static final String CONFIG_DIR_PATH = "./configs";

    @FXML private ComboBox<ConfigPreset> presetComboBox;
    @FXML private TextField configNameField;
    @FXML private Label configInfo;

    @FXML private Spinner<Integer> mapWidthSpinner;
    @FXML private Spinner<Integer> mapHeightSpinner;
    @FXML private Spinner<Integer> initialPlantsNumSpinner;
    @FXML private Spinner<Integer> plantMinEnergySpinner;
    @FXML private Spinner<Integer> plantMaxEnergySpinner;
    @FXML private Spinner<Integer> dailyPlantGrowthSpinner;
    @FXML private Spinner<Integer> initialAnimalsNumSpinner;
    @FXML private Spinner<Integer> animalEnergyLossSpinner;
    @FXML private Spinner<Integer> animalStartEnergySpinner;
    @FXML private Spinner<Integer> energyNeededToReproduceSpinner;
    @FXML private Spinner<Integer> energyUsedToReproduceSpinner;
    @FXML private Spinner<Integer> minMutationsSpinner;
    @FXML private Spinner<Integer> maxMutationsSpinner;
    @FXML private Spinner<Integer> genomeLengthSpinner;

    @FXML private CheckBox isFomoCheckBox;
    @FXML private Spinner<Integer> fomoRadiusSpinner;
    @FXML private Spinner<Integer> fomoCrowdSizeSpinner;

    @FXML private Label errorLabel;

    private final SimulationEngine simulationEngine = new SimulationEngine();
    private ConfigurationManager configManager;

    // INITIALIZATION //

    @FXML
    public void initialize() {
        this.configManager = new ConfigurationManager("configs");

        loadPresets();

        presetComboBox.setOnAction(event -> {
            ConfigPreset selected = presetComboBox.getValue();
            if (selected != null) {
                updateGuiFromParams(selected.config());
                configInfo.setTextFill(Color.GREEN);
                configInfo.setText("Wczytano preset: " + selected);
            }
        });

        setupRangeValidation(minMutationsSpinner, maxMutationsSpinner);
        setupRangeValidation(plantMinEnergySpinner, plantMaxEnergySpinner);
    }

    // FXML HANDLERS //

    @FXML
    private void onStartClicked() {
        Config config;

        try {
            config = getConfig();
        } catch (Exception e) {
            errorLabel.setText("Błąd konfiguracji: " + e.getMessage());
            return;
        }

        try {
            createNewSimulation(config);
            errorLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Błąd startu symulacji: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveConfigClicked() {
        String configName = configNameField.getText().trim();

        if (configName.isEmpty()) {
            configInfo.setTextFill(Color.RED);
            configInfo.setText("Podaj nazwę konfiguracji przed zapisem!");
            return;
        }

        try {
            Config currentConfig = getConfig();
            configManager.saveConfig(currentConfig, configName);
            loadPresets();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Błąd zapisu %s%n".formatted(e.getMessage()));
        }
    }

    // APP LOGIC //

    private void createNewSimulation(Config config) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SimulationView.fxml"));
        BorderPane root = fxmlLoader.load();

        SimulationController presenter = fxmlLoader.getController();
        Simulation simulation = new Simulation(config);

        Stage simulationStage = new Stage();
        simulationStage.setTitle("Symulacja ID: " + simulation.getId());
        simulationStage.setScene(new Scene(root));

        presenter.setConfig(config);
        presenter.setSimulation(simulation);

        simulationEngine.runAsync(simulation);
        simulationStage.show();

        simulationStage.setOnCloseRequest(event -> {
            System.out.println("Zamykanie symulacji ID: " + simulation.getId());
            simulation.end();

        });
    }

    private void loadPresets() {
        List<ConfigPreset> presets = new ArrayList<>();
        presets.addAll(createDefaultPresets());
        presets.addAll(loadUserPresets());

        presetComboBox.getItems().setAll(presets);
    }

    // DATA HELPERS //

    private List<ConfigPreset> createDefaultPresets() {
        return List.of(
                new ConfigPreset("Standardowy (20x20)", new Config(
                        20, 20, 15, 5, 10, 2, 15, 1, 20, 10, 5, 0, 2, 5, false, 5, 3
                )),
                new ConfigPreset("Gigant (50x50)", new Config(
                        50, 50, 100, 5, 10, 10, 80, 2, 50, 20, 10, 0, 5, 8, false, 5, 3
                )),
                new ConfigPreset("Walka o przetrwanie", new Config(
                        15, 15, 5, 2, 5, 1, 20, 5, 30, 25, 10, 2, 6, 10, true, 5, 5
                ))
        );
    }

    private List<ConfigPreset> loadUserPresets() {
        List<ConfigPreset> userPresets = new ArrayList<>();
        File folder = new File(CONFIG_DIR_PATH);

        if (!folder.exists() || !folder.isDirectory()) {
            return userPresets;
        }

        File[] configFiles = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (configFiles != null) {
            for (File file : configFiles) {
                try {
                    Config loadedConfig = configManager.loadConfig(file.getName());
                    userPresets.add(new ConfigPreset("Plik: " + file.getName(), loadedConfig));
                } catch (IOException e) {
                    errorLabel.setText("Ignoruję uszkodzony plik konfiguracyjny: " + file.getName());
                }
            }
        }
        return userPresets;
    }

    // UI HELPERS //

    private Config getConfig() {
        return new Config(
                mapWidthSpinner.getValue(),
                mapHeightSpinner.getValue(),
                initialPlantsNumSpinner.getValue(),
                plantMinEnergySpinner.getValue(),
                plantMaxEnergySpinner.getValue(),
                dailyPlantGrowthSpinner.getValue(),
                initialAnimalsNumSpinner.getValue(),
                animalEnergyLossSpinner.getValue(),
                animalStartEnergySpinner.getValue(),
                energyNeededToReproduceSpinner.getValue(),
                energyUsedToReproduceSpinner.getValue(),
                minMutationsSpinner.getValue(),
                maxMutationsSpinner.getValue(),
                genomeLengthSpinner.getValue(),
                isFomoCheckBox.isSelected(),
                fomoRadiusSpinner.getValue(),
                fomoCrowdSizeSpinner.getValue()
        );
    }

    private void updateGuiFromParams(Config config) {
        setSpinnerValue(mapWidthSpinner, config.mapWidth());
        setSpinnerValue(mapHeightSpinner, config.mapHeight());

        setSpinnerValue(initialPlantsNumSpinner, config.initialPlantsNum());
        setSpinnerValue(plantMinEnergySpinner, config.plantMinEnergy());
        setSpinnerValue(plantMaxEnergySpinner, config.plantMaxEnergy());
        setSpinnerValue(dailyPlantGrowthSpinner, config.dailyPlantGrowth());

        setSpinnerValue(initialAnimalsNumSpinner, config.initialAnimalsNum());
        setSpinnerValue(animalStartEnergySpinner, config.animalStartEnergy());
        setSpinnerValue(animalEnergyLossSpinner, config.animalEnergyLoss());
        setSpinnerValue(energyNeededToReproduceSpinner, config.energyNeededToReproduce());
        setSpinnerValue(energyUsedToReproduceSpinner, config.energyUsedToReproduce());

        setSpinnerValue(minMutationsSpinner, config.minMutations());
        setSpinnerValue(maxMutationsSpinner, config.maxMutations());
        setSpinnerValue(genomeLengthSpinner, config.genomLength());

        isFomoCheckBox.setSelected(config.isFomo());
        setSpinnerValue(fomoRadiusSpinner, config.fomoRadius());
        setSpinnerValue(fomoCrowdSizeSpinner, config.fomoCrowdSize());
    }

    private void setSpinnerValue(Spinner<Integer> spinner, int value) {
        spinner.getValueFactory().setValue(value);
    }

    // Range validators

    private void setupRangeValidation(Spinner<Integer> minSpinner, Spinner<Integer> maxSpinner) {
        minSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateAndSwap(minSpinner, maxSpinner);
            }
        });

        maxSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateAndSwap(minSpinner, maxSpinner);
            }
        });
    }

    private void validateAndSwap(Spinner<Integer> minSpinner, Spinner<Integer> maxSpinner) {
        commitValue(minSpinner);
        commitValue(maxSpinner);

        Integer min = minSpinner.getValue();
        Integer max = maxSpinner.getValue();

        if (min == null) min = 0;
        if (max == null) max = 0;

        if (min > max) {
            minSpinner.getValueFactory().setValue(max);
            maxSpinner.getValueFactory().setValue(min);
        }
    }

    private void commitValue(Spinner<Integer> spinner) {
        if (!spinner.isEditable()) return;

        String text = spinner.getEditor().getText();
        SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();

        if (valueFactory != null) {
            StringConverter<Integer> converter = valueFactory.getConverter();
            if (converter != null) {
                try {
                    Integer value = converter.fromString(text);
                    valueFactory.setValue(value);
                } catch (Exception e) {
                    spinner.getEditor().setText(converter.toString(valueFactory.getValue()));
                }
            }
        }
    }
}