package com.MoziCity.mozi_jegyfoglalo.scenes;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeatSelectionScene extends Scene {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 700;
    private final GridPane seatGrid = new GridPane();
    // FONTOS: ArrayList helyett ObservableList-et használunk
    private final ObservableList<Seat> allSeats = FXCollections.observableArrayList();
    private final ObservableList<Seat> selectedSeats = FXCollections.observableArrayList();
    private final Movie movie;
    private final MainApp mainApp;

    public SeatSelectionScene(Movie movie, MainApp mainApp) {
        super(new VBox(), WIDTH, HEIGHT);
        this.movie = movie;
        this.mainApp = mainApp;
        VBox root = (VBox) getRoot();

        // Stíluslap alkalmazása
        getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Cím
        Label titleLabel = new Label("Válasszon üléseket");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        // Film információ
        HBox movieInfoBox = new HBox(20);
        movieInfoBox.setAlignment(Pos.CENTER);
        movieInfoBox.setPadding(new Insets(10));

        Label movieLabel = new Label("Film: " + movie.getTitle());
        movieLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movieLabel.setTextFill(Color.WHITE);

        Label priceLabel = new Label("Jegyár: " + movie.getPrice() + " Ft");
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.web("#FFC107"));

        movieInfoBox.getChildren().addAll(movieLabel, priceLabel);

        // Mozi vászon
        Label screenLabel = new Label("VÁSZON");
        screenLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        screenLabel.setTextFill(Color.WHITE);
        screenLabel.setAlignment(Pos.CENTER);
        screenLabel.setMaxWidth(Double.MAX_VALUE);
        screenLabel.setStyle("-fx-background-color: linear-gradient(to bottom, #444, #111); -fx-padding: 10px; -fx-background-radius: 10px;");

        // Ülések
        initializeSeats();
        updateSeatGrid();

        // Jelmagyarázat
        HBox legend = createLegend();

        // Folytatás gomb
        Button continueButton = new Button("Tovább a fizetéshez");
        continueButton.getStyleClass().add("select-button");
        continueButton.setDisable(true); // Alapértelmezetten letiltva

        // JAVÍTOTT BINDING: Az ObservableList változásait most már figyelni tudja.
        // A gomb akkor van engedélyezve, ha a selectedSeats lista NEM üres.
        continueButton.disableProperty().bind(Bindings.isEmpty(selectedSeats));

        continueButton.setOnAction(e -> {
            // Átadjuk egy új ArrayList-ként a kiválasztott helyeket, hogy a következő jelenet ne módosítsa az eredeti listát.
            mainApp.showConfirmationScene(movie, new ArrayList<>(selectedSeats));
        });

        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(titleLabel, movieInfoBox, screenLabel, seatGrid, legend, continueButton);
        root.getStyleClass().add("root");
    }

    private void initializeSeats() {
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int seatsPerRow = 12;
        Random random = new Random();

        for (String row : rows) {
            for (int i = 1; i <= seatsPerRow; i++) {
                // Véletlenszerűen elfoglalt helyek generálása a demóhoz
                SeatStatus status = (random.nextDouble() < 0.3) ? SeatStatus.TAKEN : SeatStatus.FREE;
                allSeats.add(new Seat(row, i, status));
            }
        }
    }

    private void updateSeatGrid() {
        seatGrid.getChildren().clear();
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setPadding(new Insets(20));

        int colIndex = 0;
        for (Seat seat : allSeats) {
            Button seatButton = new Button(seat.getSeatId());
            seatButton.setPrefSize(40, 40);
            seatButton.setFont(Font.font(10));

            updateSeatButtonStyle(seatButton, seat.getStatus());

            seatButton.setOnAction(e -> handleSeatClick(seat, seatButton));

            // Sor és oszlop meghatározása
            int rowIndex = seat.getRow().charAt(0) - 'A';
            int currentCol = seat.getNumber() - 1;
            seatGrid.add(seatButton, currentCol, rowIndex);
        }
    }

    private void handleSeatClick(Seat seat, Button seatButton) {
        if (seat.getStatus() == SeatStatus.TAKEN) {
            return; // A foglalt helyeket nem lehet megnyomni
        }

        if (seat.getStatus() == SeatStatus.FREE) {
            seat.setStatus(SeatStatus.SELECTED);
            selectedSeats.add(seat);
        } else { // SELECTED
            seat.setStatus(SeatStatus.FREE);
            selectedSeats.remove(seat);
        }
        updateSeatButtonStyle(seatButton, seat.getStatus());
    }

    private void updateSeatButtonStyle(Button button, SeatStatus status) {
        button.getStyleClass().removeAll("seat-free", "seat-selected", "seat-taken");
        switch (status) {
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

    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(10));

        VBox freeBox = new VBox(5);
        freeBox.setAlignment(Pos.CENTER);
        Pane freePane = new Pane();
        freePane.setPrefSize(20, 20);
        freePane.getStyleClass().add("seat-free");
        Label freeLabel = new Label("Szabad");
        freeLabel.setFont(Font.font(14));
        freeLabel.setTextFill(Color.WHITE);
        freeBox.getChildren().addAll(freePane, freeLabel);

        VBox selectedBox = new VBox(5);
        selectedBox.setAlignment(Pos.CENTER);
        Pane selectedPane = new Pane();
        selectedPane.setPrefSize(20, 20);
        selectedPane.getStyleClass().add("seat-selected");
        Label selectedLabel = new Label("Kiválasztva");
        selectedLabel.setFont(Font.font(14));
        selectedLabel.setTextFill(Color.WHITE);
        selectedBox.getChildren().addAll(selectedPane, selectedLabel);

        VBox takenBox = new VBox(5);
        takenBox.setAlignment(Pos.CENTER);
        Pane takenPane = new Pane();
        takenPane.setPrefSize(20, 20);
        takenPane.getStyleClass().add("seat-taken");
        Label takenLabel = new Label("Foglalt");
        takenLabel.setFont(Font.font(14));
        takenLabel.setTextFill(Color.WHITE);
        takenBox.getChildren().addAll(takenPane, takenLabel);

        legend.getChildren().addAll(freeBox, selectedBox, takenBox);
        return legend;
    }
}