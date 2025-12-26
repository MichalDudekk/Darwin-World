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

public class ConfigurationManager {
    private final String folderPath;
    private final Gson gson;

    public ConfigurationManager(String folderPath) {
        this.folderPath = folderPath;

        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void createConfigFolder() {
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Nie udało się utworzyć folderu konfiguracji: " + e.getMessage()));
        }
    }

    public void saveConfig(Params config, String filename) {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String fullPath = folderPath + "/" + filename;

        try (FileWriter writer = new FileWriter(fullPath)) {
            gson.toJson(config, writer);
            System.err.printf("Zapisano konfigutracje do: %s%n", fullPath);
        } catch (IOException e) {
            System.err.println("Błąd zapisu konfiguracji: " + e.getMessage());
        }
    }

    public Params loadConfig(String filename) {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String fullPath = folderPath + "/" + filename;

        try (FileReader reader = new FileReader(fullPath)) {
            return gson.fromJson(reader, Params.class);
        } catch (IOException e) {
            System.err.println("Błąd odczytu konfiguracji: " + e.getMessage());
            return null;
        }
    }

    public String getFolderPath() {
        return folderPath;
    }
}
