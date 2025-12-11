package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.MoziCity.mozi_jegyfoglalo.service.OmdbService;
import javafx.application.Platform;

public class AddMovieController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imageUrlField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField priceField;
    @FXML private Label statusLabel;

    private MainApp mainApp;
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = new DatabaseManager();
        statusLabel.setText("");
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleAutoFill() {
        String title = titleField.getText();


        if (title == null || title.trim().isEmpty()) {
            showError("Kérlek, írd be a film címét a kereséshez!");
            return;
        }


        statusLabel.setTextFill(Color.YELLOW);
        statusLabel.setText("Keresés az adatbázisban...");


        new Thread(() -> {

            OmdbService.MovieData data = OmdbService.searchMovieByTitle(title);


            Platform.runLater(() -> {
                if (data != null) {

                    if (descriptionArea.getText().isEmpty()) {
                        descriptionArea.setText(data.description);
                    } else {

                        descriptionArea.setText(data.description);
                    }

                    if (imageUrlField.getText().isEmpty()) {
                        imageUrlField.setText(data.imageUrl);
                    } else {
                        imageUrlField.setText(data.imageUrl);
                    }

                    showSuccess("Adatok sikeresen letöltve!");
                } else {
                    showError("Nem található film ezzel a címmel (vagy hiba történt).");
                }
            });
        }).start();
    }


    @FXML
    private void handleSave() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String imageUrl = imageUrlField.getText();
        LocalDate date = datePicker.getValue();
        String time = timeField.getText();
        String priceStr = priceField.getText();

        if (title == null || title.isEmpty() || date == null ||
                time == null || time.isEmpty() || priceStr == null || priceStr.isEmpty()) {
            showError("Minden csillagozott mező kitöltése kötelező!");
            return;
        }

        if (!time.matches("\\d{2}:\\d{2}")) {
            showError("Helytelen időpont formátum! (Helyes: HH:mm)");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showError("Helytelen ár formátum! (Csak számot adjon meg)");
            return;
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        boolean success = dbManager.addNewMovieAndShowtime(title, description, imageUrl, dateStr, time, price);

        if (success) {
            showSuccess("Film sikeresen hozzáadva!");
            titleField.clear();
            descriptionArea.clear();
            imageUrlField.clear();
            datePicker.setValue(null);
            timeField.clear();
            priceField.clear();
        } else {
            showError("Hiba történt a mentés során. Ellenőrizze a konzolt.");
        }
    }

    @FXML
    private void handleBack() {
        mainApp.showMovieSelectionScene();
    }

    private void showError(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText(message);
    }
}