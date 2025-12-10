package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import com.MoziCity.mozi_jegyfoglalo.model.CartItem;
import com.MoziCity.mozi_jegyfoglalo.service.CartService;

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
    private List<Seat> seats;
    private List<Seat> selectedSeats;

    private CartService cartService = new CartService();

    @FXML
    public void initialize() {
        selectedSeats = new ArrayList<>();
        continueButton.setDisable(true);
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
        seats = new ArrayList<>();
        for (char row = 'A'; row <= 'H'; row++) {
            for (int number = 1; number <= 10; number++) {
                SeatStatus status = Math.random() < 0.2 ? SeatStatus.TAKEN : SeatStatus.FREE;
                seats.add(new Seat(String.valueOf(row), number, status));
            }
        }
        displaySeats();
    }

    private void displaySeats() {
        seatsGridPane.getChildren().clear();
        for (Seat seat : seats) {
            // 1. LÉPÉS: Összeállítjuk a szöveget
            String seatLabelText = seat.getRow() + seat.getNumber();

            // 2. LÉPÉS: DEBUG - Kiírjuk a konzolra, mi a szöveg
            // Futtasd a programot, és nézd meg a konzolt (a kimeneti ablakot az IDE-ben).
            // Itt látnod kell, hogy "A1", "A2" stb. szövegek jönnek-e létre.
            System.out.println("DEBUG: Creating button with text: " + seatLabelText);

            // 3. LÉPÉS: Létrehozzuk a gombot a szöveggel
            Button seatButton = new Button(seatLabelText);

            // 4. LÉPÉS: Beállítjuk a méretet - NÖVELTÜK a méretet, hogy biztosan elférjen a szöveg
            seatButton.setPrefSize(50, 50); // Méret növelése 40x50-ről 50x50-re
            seatButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Ne legyen kisebb a beállított méretnél
            seatButton.setMaxSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Ne legyen nagyobb a beállított méretnél

            updateSeatButtonStyle(seatButton, seat);
            seatButton.setOnAction(e -> handleSeatClick(seat, seatButton));

            int rowIndex = seat.getRow().charAt(0) - 'A';
            int colIndex = seat.getNumber() - 1;
            seatsGridPane.add(seatButton, colIndex, rowIndex);
        }
    }

    private void updateSeatButtonStyle(Button button, Seat seat) {
        button.getStyleClass().removeAll("seat-free", "seat-selected", "seat-taken");
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
                button.setDisable(true);
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
            cartService.clearCart();

            for (Seat seat : selectedSeats) {
                cartService.addToCart(
                        new CartItem(
                                movie.getTitle(),
                                seat.getSeatId(),
                                2500
                        )
                );
            }
            System.out.println("Kosár tartalma:");
            System.out.println("Tételek száma: " + cartService.getItemCount());
            System.out.println("Végösszeg: " + cartService.getTotalPrice() + " Ft");

            mainApp.showConfirmationScene(movie, selectedSeats);
        }
    }
}