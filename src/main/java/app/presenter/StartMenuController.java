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
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class StartMenuController {
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
    private void onStartClicked() {
        try {
            Config config = getConfig();

            System.out.println("--- Simulation configured ---");
            System.out.printf("Map: %sx%s%n", config.mapWidth(), config.mapHeight());
            System.out.println("FOMO Mode: " + config.isFomo());
            System.out.println("Params Object: " + config);

            createNewSimulation(config);

            // TODO: Po odpaleniu kilka razy symulacji się sypie

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