package com.MoziCity.mozi_jegyfoglalo.controller;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField; // FONTOS IMPORT

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ConfirmationController {
    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label showtimeLabel;

    @FXML
    private Label seatsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private TextField nameTextField; // EZT KERESTE AZ FXML

    @FXML
    private TextField emailTextField; // EZT IS KERESTE AZ FXML

    private MainApp mainApp;
    private Movie movie;
    private List<Seat> selectedSeats;
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = new DatabaseManager();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setBookingDetails(Movie movie, List<Seat> selectedSeats) {
        this.movie = movie;
        this.selectedSeats = selectedSeats;

        updateBookingDetails();
    }
    public void setDbManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    private void updateBookingDetails() {
        movieTitleLabel.setText(movie.getTitle());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");
        showtimeLabel.setText(movie.getShowtime().format(formatter));

        String seatsText = selectedSeats.stream()
                .map(Seat::getSeatId)
                .collect(Collectors.joining(", "));
        seatsLabel.setText(seatsText);

        int totalPrice = movie.getPrice() * selectedSeats.size();
        totalPriceLabel.setText(totalPrice + " Ft");
    }

    @FXML
    private void handleBack() {
        mainApp.showSeatSelectionScene(movie);
    }

    @FXML
    public void handleConfirm() {

        // 1. Adatok lekérése és validálása
        String customerName = nameTextField.getText().trim();
        String customerEmail = emailTextField.getText().trim();

        if (customerName.isEmpty()) {
            mainApp.showError("Hiányzó adat", "Kérjük, adja meg a nevét a foglaláshoz!");
            return; // Megállítjuk a folyamatot
        }

        // Alapvető e-mail validáció
        if (customerEmail.isEmpty() || !customerEmail.contains("@") || !customerEmail.contains(".")) {
            mainApp.showError("Érvénytelen adat", "Kérjük, adjon meg egy érvényes e-mail címet!");
            return; // Megállítjuk a folyamatot
        }

        int totalPrice = movie.getPrice() * selectedSeats.size();

        // 2. Megpróbáljuk elmenteni a foglalást az adatbázisba
        // FIGYELEM: Ehhez módosítanod kell a DatabaseManager.java-t!
        // (A metódus aláírását, hogy fogadja a nevet és emailt)
        boolean success = dbManager.saveBooking(movie.getVetitesId(), selectedSeats, totalPrice, customerName, customerEmail);

        if (success) {
            // 3. SIKERES MENTÉS ESETÉN (Üzenet kiegészítve a névvel)
            String seatsText = selectedSeats.stream().map(Seat::getSeatId).collect(Collectors.joining(", "));
            mainApp.showInfo("Sikeres foglalás",
                    "Köszönjük a foglalását, " + customerName + "!\n" + // Név hozzáadva
                            "A visszaigazolást hamarosan elküldjük (" + customerEmail + ").\n\n" +
                            "Film: " + movie.getTitle() + "\n" +
                            "Időpont: " + movie.getShowtime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")) + "\n" +
                            "Helyek: " + seatsText + "\n" +
                            "Összesen: " + totalPrice + " Ft");

            // Vissza a filmválasztó képernyőre
            mainApp.showMovieSelectionScene();

        } else {
            // 4. SIKERTELEN MENTÉS ESETÉN
            mainApp.showError("Foglalási hiba",
                    "Sajnos hiba történt a foglalás mentése közben. Kérjük, próbálja újra később.");
        }
    }
}