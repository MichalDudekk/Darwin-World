package app.presenter;

import app.shared.Config;
import app.simulation.ConfigPreset;
import app.simulation.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PresetManager {
    private final String configDirPath;
    private final ConfigurationManager configManager;

    public PresetManager(String configDirPath, ConfigurationManager configManager) {
        this.configDirPath = configDirPath;
        this.configManager = configManager;
    }

    public List<ConfigPreset> loadAllPresets() {
        List<ConfigPreset> presets = new ArrayList<>();
        presets.addAll(createDefaultPresets());
        presets.addAll(loadUserPresets());
        return presets;
    }

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
        File folder = new File(configDirPath);

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
                    System.err.println("Błąd ładowania pliku: " + file.getName());
                }
            }
        }
        return userPresets;
    }
}