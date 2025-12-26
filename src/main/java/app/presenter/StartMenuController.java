package app.presenter;

import app.simulation.ConfigurationManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StartMenuController {
    @FXML private TextField configNameField;

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

    private final ConfigurationManager configManager = new ConfigurationManager("configs");

    @FXML
    private void onStartClicked() {
        try {
            Params params = new Params(
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

            System.out.println("--- Simulation configured ---");
            System.out.printf("Map: %sx%s%n", params.MapWidth(), params.mapHeigth());
            System.out.println("FOMO Mode: " + params.isFomo());
            System.out.println("Params Object: " + params);

            // TODO: Wywołanie prezentera symulacji

        } catch (NumberFormatException e) {
            errorLabel.setText("Błąd: Wszystkie pola liczbowe muszą być liczbami całkowitymi!");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Błąd walidacji: " + e.getMessage());
        }
    }



    private int getInt(TextField field) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Pola nie mogą być puste");
        }
        return Integer.parseInt(text.trim());
    }
}