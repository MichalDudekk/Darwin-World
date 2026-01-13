package app.simulation;

import app.shared.Config;

public record ConfigPreset(
        String name,
        Config config
) {
    @Override
    public String toString() {
        return name;
    }
}
