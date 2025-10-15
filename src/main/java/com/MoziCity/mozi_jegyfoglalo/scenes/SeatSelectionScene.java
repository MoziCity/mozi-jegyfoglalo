package com.MoziCity.mozi_jegyfoglalo.scenes;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static final double WIDTH = 800;
    private static final double HEIGHT = 700;
    private final GridPane seatGrid = new GridPane();
    private final List<Seat> allSeats = new ArrayList<>();
    private final List<Seat> selectedSeats = new ArrayList<>();
    private final Movie movie;
    private final MainApp mainApp;

    public SeatSelectionScene(Movie movie, MainApp mainApp) {
        super(new VBox(20), WIDTH, HEIGHT);
        this.movie = movie;
        this.mainApp = mainApp;
        VBox root = (VBox) getRoot();

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

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
        continueButton.getStyleClass().add("action-button");
        continueButton.setDisable(true); // Alapértelmezetten letiltva

        // Gomb engedélyezése/letiltása a kiválasztott ülések alapján
        BooleanProperty hasSelection = new SimpleBooleanProperty(false);
        hasSelection.bind(Bindings.createBooleanBinding(() -> !selectedSeats.isEmpty(), selectedSeats));
        continueButton.disableProperty().bind(hasSelection.not());

        continueButton.setOnAction(e -> mainApp.showConfirmationScene(movie, new ArrayList<>(selectedSeats)));

        root.getChildren().addAll(screenLabel, seatGrid, legend, continueButton);
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

        Label freeLabel = new Label("Szabad");
        freeLabel.getStyleClass().add("legend-label");
        Pane freePane = new Pane();
        freePane.setPrefSize(20, 20);
        freePane.getStyleClass().add("seat-free");

        Label selectedLabel = new Label("Kiválasztva");
        selectedLabel.getStyleClass().add("legend-label");
        Pane selectedPane = new Pane();
        selectedPane.setPrefSize(20, 20);
        selectedPane.getStyleClass().add("seat-selected");

        Label takenLabel = new Label("Foglalt");
        takenLabel.getStyleClass().add("legend-label");
        Pane takenPane = new Pane();
        takenPane.setPrefSize(20, 20);
        takenPane.getStyleClass().add("seat-taken");

        legend.getChildren().addAll(freePane, freeLabel, selectedPane, selectedLabel, takenPane, takenLabel);
        return legend;
    }
}