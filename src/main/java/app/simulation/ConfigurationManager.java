package app.simulation;

import app.presenter.Params;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class ConfigurationManager {
    private final String folderPath;
    private final Gson gson;
    private final Label infoField;

    public ConfigurationManager(String folderPath, Label infoField) {
        this.folderPath = folderPath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.infoField = infoField;
    }

    private void createConfigFolder() {
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            infoField.setTextFill(Color.RED);
            infoField.setText("Nie udało się utworzyć folderu konfiguracji: " + e.getMessage());
        }
    }

    public void saveConfig(Params config, String filename) {
        createConfigFolder();

        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String fullPath = folderPath + "/" + filename;

        try (FileWriter writer = new FileWriter(fullPath)) {
            gson.toJson(config, writer);
            infoField.setTextFill(Color.GREEN);
            infoField.setText("Zapisano konfiguracje do: %s".formatted(fullPath));
        } catch (IOException e) {
            infoField.setTextFill(Color.RED);
            infoField.setText("Błąd zapisu konfiguracji: %s".formatted(e.getMessage()));
        }
    }

    public Params loadConfig(String filename) {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String fullPath = folderPath + "/" + filename;

        try (FileReader reader = new FileReader(fullPath)) {
            Params read = gson.fromJson(reader, Params.class);

            infoField.setTextFill(Color.GREEN);
            infoField.setText("Wczytano konfiguracje z: %s".formatted(fullPath));

            return read;
        } catch (IOException e) {
            infoField.setTextFill(Color.RED);
            infoField.setText("Błąd odczytu konfiguracji: %s".formatted(e.getMessage()));
            return null;
        }
    }

    public String getFolderPath() {
        return folderPath;
    }
}
