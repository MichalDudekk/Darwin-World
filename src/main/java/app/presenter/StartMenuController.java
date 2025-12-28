package app.presenter;

import app.shared.Config;
import app.simulation.ConfigurationManager;
import app.simulation.Simulation;
import app.simulation.SimulationEngine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class StartMenuController {
    @FXML private TextField configNameField;
    @FXML private Label configInfo;

    @FXML private TextField mapWidthField;
    @FXML private TextField mapHeightField;
    @FXML private TextField initialPlantsNumField;
    @FXML private TextField plantMinEnergyField;
    @FXML private TextField plantMaxEnergyField;
    @FXML private TextField dailyPlantGrowthField;
    @FXML private TextField initialAnimalsNumField;
    @FXML private TextField animalEnergyLossField;
    @FXML private TextField energyNeededToReproduceField;
    @FXML private TextField energyUsedToReproduceField;
    @FXML private TextField minMutationsField;
    @FXML private TextField maxMutationsField;
    @FXML private TextField genomeLengthField;

    @FXML private CheckBox isFomoCheckBox;
    @FXML private TextField fomoRadiusField;
    @FXML private TextField fomoCrowdSizeField;

    @FXML private Label errorLabel;

    private final SimulationEngine simulationEngine = new SimulationEngine();
    private ConfigurationManager configManager;

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

            presenter.setSimulation(simulation);
            presenter.setConfig(config);

            simulationEngine.runAsync(simulation);

        } catch (IOException e) {
            errorLabel.setText("Błąd podczas tworzenia symualcji: " + e.getMessage());
        }

    }

    @FXML
    public void initialize() {
        this.configManager = new ConfigurationManager("configs", configInfo);
    }

    @FXML
    private void onSaveConfigClicked() {
        try {
            Config currentConfig = getConfig();
            configManager.saveConfig(currentConfig, configNameField.getText());
        } catch (Exception e) {
            errorLabel.setText("Błąd zapisu %s%n".formatted(e.getMessage()));
        }
    }

    @FXML
    private void onLoadConfigClicked() {
        Config loadedConfig = configManager.loadConfig(configNameField.getText());

        if (loadedConfig != null) {
            updateGuiFromParams(loadedConfig);
        }
    }

    private void updateGuiFromParams(Config config) {
        mapWidthField.setText(String.valueOf(config.mapWidth()));
        mapHeightField.setText(String.valueOf(config.mapHeight()));
        initialAnimalsNumField.setText(String.valueOf(config.initialAnimalsNum()));
        plantMinEnergyField.setText(String.valueOf(config.plantMinEnergy()));
        plantMaxEnergyField.setText(String.valueOf(config.plantMaxEnergy()));
        dailyPlantGrowthField.setText(String.valueOf(config.dailyPlantGrowth()));
        initialAnimalsNumField.setText(String.valueOf(config.initialAnimalsNum()));
        animalEnergyLossField.setText(String.valueOf(config.animalEnergyLoss()));
        energyNeededToReproduceField.setText(String.valueOf(config.energyNeededToReproduce()));
        energyUsedToReproduceField.setText(String.valueOf(config.energyUsedToReproduce()));
        minMutationsField.setText(String.valueOf(config.minMutations()));
        maxMutationsField.setText(String.valueOf(config.maxMutations()));
        genomeLengthField.setText(String.valueOf(config.genomLength()));
        isFomoCheckBox.setSelected(config.isFomo());
        fomoRadiusField.setText(String.valueOf(config.fomoRadius()));
        fomoCrowdSizeField.setText(String.valueOf(config.fomoCrowdSize()));
    }

    private int getInt(TextField field) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Pola nie mogą być puste");
        }
        return Integer.parseInt(text.trim());
    }

    private Config getConfig() {
        return new Config(
                getInt(mapWidthField),
                getInt(mapHeightField),
                getInt(initialPlantsNumField),
                getInt(plantMinEnergyField),
                getInt(plantMaxEnergyField),
                getInt(dailyPlantGrowthField),
                getInt(initialAnimalsNumField),
                getInt(animalEnergyLossField),
                getInt(energyNeededToReproduceField),
                getInt(energyUsedToReproduceField),
                getInt(minMutationsField),
                getInt(maxMutationsField),
                getInt(genomeLengthField),
                isFomoCheckBox.isSelected(),
                getInt(fomoRadiusField),
                getInt(fomoCrowdSizeField)
        );
    }
}