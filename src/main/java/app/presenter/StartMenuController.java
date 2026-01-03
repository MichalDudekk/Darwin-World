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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartMenuController {
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

    @FXML
    public void initialize() {
        this.configManager = new ConfigurationManager("configs", configInfo);

        loadPresets();

        presetComboBox.setOnAction(event -> {
            ConfigPreset selected = presetComboBox.getValue();
            if (selected != null) {
                updateGuiFromParams(selected.config());
                configInfo.setText("Wczytano preset: " + selected);
            }
        });
    }

    private void loadPresets() {
        List<ConfigPreset> presets = new ArrayList<>();

        presets.add(new ConfigPreset("Standardowy (20x20)", new Config(
                20, 20, 15, 5, 10, 2, 15, 1, 20, 10, 5, 0, 2, 5, false, 5, 3
        )));

        presets.add(new ConfigPreset("Gigant (50x50)", new Config(
                50, 50, 100, 5, 10, 10, 80, 2, 50, 20, 10, 0, 5, 8, false, 5, 3
        )));

        presets.add(new ConfigPreset("Walka o przetrwanie", new Config(
                15, 15, 5, 2, 5, 1, 20, 5, 30, 25, 10, 2, 6, 10, true, 5, 5
        )));

        File folder = new File("./configs");
        File[] configFiles = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (configFiles != null) {
            for (File file : configFiles) {
                try {
                    Config loadedConfig = loadConfigFromFile(file.getName());

                    presets.add(new ConfigPreset("Plik: " + file.getName(), loadedConfig));
                } catch (Exception e) {
                    System.err.println("Nie udało się wczytać presetu: " + file.getName());
                }
            }
        }

        presetComboBox.getItems().setAll(presets);
    }

    @FXML
    private void onStartClicked() {
        try {
            Config config = getConfig();

            System.out.println("--- Simulation configured ---");
            System.out.printf("Map: %sx%s%n", config.mapWidth(), config.mapHeight());
            System.out.println("FOMO Mode: " + config.isFomo());
            System.out.println("Params Object: " + config);

            createNewSimulation(config);

        } catch (NumberFormatException e) {
            errorLabel.setText("Błąd: Wszystkie pola liczbowe muszą być liczbami całkowitymi!");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Błąd walidacji: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Błąd: " + e.getMessage());
        }
    }

    private void createNewSimulation(Config config) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SimulationView.fxml"));

        try {
            BorderPane root = fxmlLoader.load();

            SimulationController presenter = fxmlLoader.getController();

            Simulation simulation = new Simulation(config);

            Stage simulationStage = new Stage();
            simulationStage.setTitle("Symulacja ID: " + simulation.getId());
            simulationStage.setScene(new Scene(root));
            simulationStage.show();

            presenter.setConfig(config);
            presenter.setSimulation(simulation);

            simulationEngine.runAsync(simulation);

            simulationStage.setOnCloseRequest((WindowEvent event) -> {
                simulation.end();
            });

        } catch (IOException e) {
            errorLabel.setText("Błąd podczas tworzenia symualcji: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void onSaveConfigClicked() {
        try {
            Config currentConfig = getConfig();
            configManager.saveConfig(currentConfig, configNameField.getText());
            loadPresets();
        } catch (Exception e) {
            errorLabel.setText("Błąd zapisu %s%n".formatted(e.getMessage()));
        }
    }

    private Config loadConfigFromFile(String configName) {
        return configManager.loadConfig(configName);
    }

    private void updateGuiFromParams(Config config) {
        setSpinnerValue(mapWidthSpinner, config.mapWidth());
        setSpinnerValue(mapHeightSpinner, config.mapHeight());

        setSpinnerValue(initialPlantsNumSpinner, config.initialPlantsNum());
        setSpinnerValue(plantMinEnergySpinner, config.plantMinEnergy());
        setSpinnerValue(plantMaxEnergySpinner, config.plantMaxEnergy());
        setSpinnerValue(dailyPlantGrowthSpinner, config.dailyPlantGrowth());

        setSpinnerValue(initialAnimalsNumSpinner, config.initialAnimalsNum());
        setSpinnerValue(animalStartEnergySpinner, config.animalStartEnergy()); // Dodałem to, bo brakowało w Twojej liście (był duplikat animalsNum)
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

    private int getInt(Spinner<Integer> spinner) {
        return spinner.getValue();
    }

    private Config getConfig() {
        return new Config(
                getInt(mapWidthSpinner),
                getInt(mapHeightSpinner),
                getInt(initialPlantsNumSpinner),
                getInt(plantMinEnergySpinner),
                getInt(plantMaxEnergySpinner),
                getInt(dailyPlantGrowthSpinner),
                getInt(initialAnimalsNumSpinner),
                getInt(animalEnergyLossSpinner),
                getInt(animalStartEnergySpinner),
                getInt(energyNeededToReproduceSpinner),
                getInt(energyUsedToReproduceSpinner),
                getInt(minMutationsSpinner),
                getInt(maxMutationsSpinner),
                getInt(genomeLengthSpinner),
                isFomoCheckBox.isSelected(),
                getInt(fomoRadiusSpinner),
                getInt(fomoCrowdSizeSpinner)
        );
    }
}