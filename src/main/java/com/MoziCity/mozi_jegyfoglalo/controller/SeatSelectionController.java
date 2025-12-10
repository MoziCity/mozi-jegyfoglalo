package com.MoziCity.mozi_jegyfoglalo.controller;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionController {
    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label showtimeLabel;

    @FXML
    private GridPane seatsGridPane;

    @FXML
    private Button continueButton;

    private MainApp mainApp;
    private Movie movie;
    private List<Seat> selectedSeats;
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        selectedSeats = new ArrayList<>();
        continueButton.setDisable(true);
        dbManager = new DatabaseManager();
    }
    @FXML
    private void handleRefresh() {
        loadMovies();
    }

    private void loadMovies() {
    }

    // EZ AZ ÚJ METÓDUS
    @FXML
    private void handleAddNewMovie() {
        mainApp.showAddMovieScene();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        updateMovieInfo();
        loadSeats();
    }

    private void updateMovieInfo() {
        movieTitleLabel.setText(movie.getTitle());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");
        showtimeLabel.setText(movie.getShowtime().format(formatter));
    }

    private void loadSeats() {
        List<Seat> seats = dbManager.getSeatsForShow(movie.getVetitesId());
        displaySeats(seats);
    }

    private void displaySeats(List<Seat> seats) {
        seatsGridPane.getChildren().clear();

        for (Seat seat : seats) {
            Button seatButton = new Button(seat.getSeatId());

            // --- MÓDOSÍTÁS KEZDTE ---
            // Töröltük a fix setPrefSize(80, 50)-et!
            // Helyette rábízzuk a CSS-re (.seat-button osztály)
            seatButton.getStyleClass().add("seat-button");
            // --- MÓDOSÍTÁS VÉGE ---

            updateSeatButtonStyle(seatButton, seat);

            seatButton.setOnAction(e -> handleSeatClick(seat, seatButton));

            int row = seat.getRow().charAt(0) - 'A';
            int col = seat.getNumber() - 1;
            seatsGridPane.add(seatButton, col, row);
        }
    }

    private void updateSeatButtonStyle(Button button, Seat seat) {
        // Először töröljük a státusz-specifikus osztályokat
        button.getStyleClass().removeAll("seat-free", "seat-selected", "seat-taken");

        // Biztosítjuk, hogy az alap "seat-button" rajta maradjon
        if (!button.getStyleClass().contains("seat-button")) {
            button.getStyleClass().add("seat-button");
        }

        switch (seat.getStatus()) {
            case FREE:
                button.getStyleClass().add("seat-free");
                button.setDisable(false);
                break;
            case SELECTED:
                button.getStyleClass().add("seat-selected");
                button.setDisable(false);
                break;
            case TAKEN:
                button.getStyleClass().add("seat-taken");
                button.setDisable(true); // A foglalt szék nem kattintható
                break;
        }
    }

    private void handleSeatClick(Seat seat, Button button) {
        if (seat.getStatus() == SeatStatus.TAKEN) return;

        if (seat.getStatus() == SeatStatus.FREE) {
            seat.setStatus(SeatStatus.SELECTED);
            selectedSeats.add(seat);
        } else {
            seat.setStatus(SeatStatus.FREE);
            selectedSeats.remove(seat);
        }

        updateSeatButtonStyle(button, seat);
        continueButton.setDisable(selectedSeats.isEmpty());
    }

    @FXML
    private void handleBack() {
        mainApp.showMovieSelectionScene();
    }

    @FXML
    private void handleContinue() {
        if (!selectedSeats.isEmpty()) {
            mainApp.showConfirmationScene(movie, selectedSeats);
        }
    }
}