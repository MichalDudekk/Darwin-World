package app.presenter;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

public class FxUtils {
    public static void configureSpinnerCommit(Spinner<?>... spinners) {
        for (Spinner<?> spinner : spinners) {
            spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    commitEditorText(spinner);
                }
            });
        }
    }

    public static void setupRangeValidation(Spinner<Integer> minSpinner, Spinner<Integer> maxSpinner) {
        minSpinner.valueProperty().addListener((obs, oldVal, newVal) -> ensureOrder(minSpinner, maxSpinner));
        maxSpinner.valueProperty().addListener((obs, oldVal, newVal) -> ensureOrder(minSpinner, maxSpinner));
    }

    private static void ensureOrder(Spinner<Integer> minSpinner, Spinner<Integer> maxSpinner) {
        if (minSpinner.getValue() > maxSpinner.getValue()) {
            int temp = minSpinner.getValue();
            minSpinner.getValueFactory().setValue(maxSpinner.getValue());
            maxSpinner.getValueFactory().setValue(temp);
        }
    }

    private static <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                try {
                    T value = converter.fromString(text);
                    valueFactory.setValue(value);
                } catch (Exception e) {
                    spinner.getEditor().setText(converter.toString(valueFactory.getValue()));
                }
            }
        }
    }
}