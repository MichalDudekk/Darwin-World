package app.simulation;

import app.shared.Config;
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

    // CONSTRUCTOR //

    public ConfigurationManager(String folderPath)  {
        this.folderPath = folderPath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // PUBLIC API //

    public void saveConfig(Config config, String filename) throws IOException {
        ensureConfigFolderExists();

        Path filePath = resolveFilePath(filename);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(config, writer);
        }
    }

    public Config loadConfig(String filename) throws IOException {
        Path filePath = resolveFilePath(filename);

        if (!Files.exists(filePath)) {
            throw new IOException("Plik nie istnieje: " + filePath);
        }

        try (FileReader reader = new FileReader(filePath.toFile())) {
            return gson.fromJson(reader, Config.class);
        }
    }

    // HELPER METHODS //

    private void ensureConfigFolderExists() throws IOException {
        Path path = Paths.get(folderPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private Path resolveFilePath(String filename) {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }
        return Paths.get(folderPath, filename);
    }
}
