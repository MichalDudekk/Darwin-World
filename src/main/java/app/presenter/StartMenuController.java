package app.presenter;

import app.simulation.ConfigurationManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

    private ConfigurationManager configManager;

    @FXML
    private void onStartClicked() {
        try {
            Params params = getParams();

            System.out.println("--- Simulation configured ---");
            System.out.printf("Map: %sx%s%n", params.mapWidth(), params.mapHeight());
            System.out.println("FOMO Mode: " + params.isFomo());
            System.out.println("Params Object: " + params);

            // TODO: Wywołanie prezentera symulacji

        } catch (NumberFormatException e) {
            errorLabel.setText("Błąd: Wszystkie pola liczbowe muszą być liczbami całkowitymi!");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Błąd walidacji: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        this.configManager = new ConfigurationManager("configs", configInfo);
    }

    @FXML
    private void onSaveConfigClicked() {
        try {
            Params currentParams = getParams();
            configManager.saveConfig(currentParams, configNameField.getText());
        } catch (Exception e) {
            errorLabel.setText("Błąd zapisu %s%n".formatted(e.getMessage()));
        }
    }

    @FXML
    private void onLoadConfigClicked() {
        Params loadedParams = configManager.loadConfig(configNameField.getText());

        if (loadedParams != null) {
            updateGuiFromParams(loadedParams);
        }
    }

    private void updateGuiFromParams(Params params) {
        mapWidthField.setText(String.valueOf(params.mapWidth()));
        mapHeightField.setText(String.valueOf(params.mapHeight()));
        initialAnimalsNumField.setText(String.valueOf(params.initialAnimalsNum()));
        plantMinEnergyField.setText(String.valueOf(params.plantMinEnergy()));
        plantMaxEnergyField.setText(String.valueOf(params.plantMaxEnergy()));
        dailyPlantGrowthField.setText(String.valueOf(params.dailyPlantGrowth()));
        initialAnimalsNumField.setText(String.valueOf(params.initialAnimalsNum()));
        animalEnergyLossField.setText(String.valueOf(params.animalEnergyLoss()));
        energyNeededToReproduceField.setText(String.valueOf(params.energyNeededToReproduce()));
        energyUsedToReproduceField.setText(String.valueOf(params.energyUsedToReproduce()));
        minMutationsField.setText(String.valueOf(params.minMutations()));
        maxMutationsField.setText(String.valueOf(params.maxMutations()));
        genomeLengthField.setText(String.valueOf(params.genomLength()));
        isFomoCheckBox.setSelected(params.isFomo());
        fomoRadiusField.setText(String.valueOf(params.fomoRadius()));
        fomoCrowdSizeField.setText(String.valueOf(params.fomoCrowdSize()));
    }

    private int getInt(TextField field) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Pola nie mogą być puste");
        }
        return Integer.parseInt(text.trim());
    }

    private Params getParams() {
        return new Params(
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