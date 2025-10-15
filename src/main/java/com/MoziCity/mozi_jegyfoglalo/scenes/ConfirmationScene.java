package com.MoziCity.mozi_jegyfoglalo.scenes;


import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ConfirmationScene extends Scene {

    private static final double WIDTH = 500;
    private static final double HEIGHT = 400;

    public ConfirmationScene(Movie movie, List<Seat> selectedSeats, MainApp mainApp) {
        super(new VBox(20), WIDTH, HEIGHT);
        VBox root = (VBox) getRoot();

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root");

        Label thankYouLabel = new Label("Sikeres foglalás!");
        thankYouLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        thankYouLabel.setTextFill(Color.web("#4CAF50"));

        Label movieLabel = new Label("Film: " + movie.getTitle());
        movieLabel.setFont(Font.font(18));
        movieLabel.setTextFill(Color.WHITE);

        StringBuilder seatsText = new StringBuilder("Helyek: ");
        for (Seat seat : selectedSeats) {
            seatsText.append(seat.getSeatId()).append(" ");
        }
        Label seatsLabel = new Label(seatsText.toString());
        seatsLabel.setFont(Font.font(18));
        seatsLabel.setTextFill(Color.WHITE);

        int totalPrice = selectedSeats.size() * movie.getPrice();
        Label priceLabel = new Label("Végösszeg: " + totalPrice + " Ft");
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        priceLabel.setTextFill(Color.web("#FFC107"));

        Button newBookingButton = new Button("Új foglalás");
        newBookingButton.getStyleClass().add("action-button");
        newBookingButton.setOnAction(e -> mainApp.showMovieSelectionScene());

        root.getChildren().addAll(thankYouLabel, movieLabel, seatsLabel, priceLabel, newBookingButton);
    }
}