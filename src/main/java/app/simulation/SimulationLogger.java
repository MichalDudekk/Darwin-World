package app.simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimulationLogger {
    private BufferedWriter writer;
    private static final String SEPARATOR = ";";

    public SimulationLogger(String prefix) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = prefix + "_" + timestamp + ".csv";

            writer = new BufferedWriter(new FileWriter(fileName));

            writeHeader();

            System.out.println("Utworzono plik logów: " + fileName);

        } catch (IOException e) {
            System.err.println("Nie udało się utworzyć pliku logów: " + e.getMessage());
        }
    }

    private void writeHeader() throws IOException {
        if (writer == null) return;
        writer.write("Dzien" + SEPARATOR);
        writer.write("Zwierzeta" + SEPARATOR);
        writer.write("Rosliny" + SEPARATOR);
        writer.write("Wolne_Pola" + SEPARATOR);
        writer.write("Sr_Energia" + SEPARATOR);
        writer.write("Sr_Zycie" + SEPARATOR);
        writer.write("Sr_Dzieci" + SEPARATOR);
        writer.write("Top_Genotyp");
        writer.newLine();
        writer.flush();
    }

    public void logDay(int day, Statistics stats) {
        if (writer == null) return;

        try {
            String topGenotype = "-";
            if (stats.topGenotypes() != null && !stats.topGenotypes().isEmpty()) {
                topGenotype = stats.topGenotypes().getFirst().getKey();
            }

            String line = String.format("%d%s%d%s%d%s%d%s%.2f%s%.2f%s%.2f%s%s",
                    day, SEPARATOR,
                    stats.animalsCount(), SEPARATOR,
                    stats.plantsCount(), SEPARATOR,
                    stats.freeFields(), SEPARATOR,
                    stats.averageEnergy(), SEPARATOR,
                    stats.averageLifespan(), SEPARATOR,
                    stats.averageChildrenCount(), SEPARATOR,
                    topGenotype
            );

            writer.write(line);
            writer.newLine();

            writer.flush();

        } catch (IOException e) {
            System.err.println("Błąd zapisu statystyk: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
                System.out.println("Zamknięto plik logów.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}