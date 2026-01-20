package app.presenter;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MapLegend {
    public void setupLegend(HBox legendContainer) {
        legendContainer.getChildren().clear();

        legendContainer.getChildren().add(createLegendItem(Color.LIMEGREEN, "Step"));
        legendContainer.getChildren().add(createLegendItem(Color.rgb(34, 139, 34), "Dżungla"));
        legendContainer.getChildren().add(createLegendItem(Color.DARKGREEN, "Roślina"));

        legendContainer.getChildren().add(new Separator(Orientation.VERTICAL));

        legendContainer.getChildren().add(createLegendItem(Color.hsb(200, 1.0, 0.5), "Min Energia"));
        legendContainer.getChildren().add(createLegendItem(Color.hsb(200, 1.0, 1.0), "Max Energia"));

        legendContainer.getChildren().add(new Separator(Orientation.VERTICAL));

        legendContainer.getChildren().add(createLegendItem(Color.WHITE, "Śledzony zwierzak"));
        legendContainer.getChildren().add(createLegendItem(Color.GOLD, "Dominujący genotyp"));
    }

    private HBox createLegendItem(Color color, String labelText) {
        HBox item = new HBox(5);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle icon = new Circle(7);
        icon.setFill(color);
        icon.setStroke(Color.GRAY);
        icon.setStrokeWidth(0.5);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px;");

        item.getChildren().addAll(icon, label);
        return item;
    }
}
