package com.MoziCity.mozi_jegyfoglalo.controller;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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
    private void handleConfirm() {
            // A "showInfo" felugró ablak helyett most már VALÓDI mentést végzünk

            int totalPrice = movie.getPrice() * selectedSeats.size();

            // Megpróbáljuk elmenteni a foglalást az adatbázisba
            boolean success = dbManager.saveBooking(movie.getVetitesId(), selectedSeats, totalPrice);

            if (success) {
                // SIKERES MENTÉS ESETÉN
                // Most már jogosan jelenítjük meg a sikeres üzenetet
                String seatsText = selectedSeats.stream().map(Seat::getSeatId).collect(Collectors.joining(", "));
                mainApp.showInfo("Sikeres foglalás",
                        "Köszönjük a foglalását!\n\n" +
                                "Film: " + movie.getTitle() + "\n" +
                                "Időpont: " + movie.getShowtime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")) + "\n" +
                                "Helyek: " + seatsText + "\n" +
                                "Összesen: " + totalPrice + " Ft");

                // Vissza a filmválasztó képernyőre
                mainApp.showMovieSelectionScene();

            } else {
                // SIKERTELEN MENTÉS ESETÉN
                mainApp.showError("Foglalási hiba",
                        "Sajnos hiba történt a foglalás mentése közben. Kérjük, próbálja újra később.");
                // (Itt maradhatunk a megerősítő képernyőn)
            }
        }
}
