package com.MoziCity.mozi_jegyfoglalo.scenes;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ConfirmationScene extends Scene {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 700;

    public ConfirmationScene(Movie movie, List<Seat> selectedSeats, MainApp mainApp) {
        super(new VBox(), WIDTH, HEIGHT);
        VBox root = (VBox) getRoot();

        // Stíluslap alkalmazása
        getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Cím
        Label titleLabel = new Label("Foglalás megerősítése");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        // Köszönő üzenet
        Label thankYouLabel = new Label("Sikeres foglalás!");
        thankYouLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        thankYouLabel.setTextFill(Color.web("#4CAF50"));

        // Film kártya
        VBox movieCard = new VBox(15);
        movieCard.setAlignment(Pos.CENTER);
        movieCard.setPadding(new Insets(20));
        movieCard.getStyleClass().add("movie-card");
        movieCard.setMaxWidth(600);

        // Film poszter
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(false);

        try {
            Image image = new Image(movie.getImageUrl(), true);
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    imageView.setImage(createPlaceholderImage());
                }
            });
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(createPlaceholderImage());
        }

        // Film információk
        Label movieLabel = new Label("Film: " + movie.getTitle());
        movieLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movieLabel.setTextFill(Color.WHITE);

        StringBuilder seatsText = new StringBuilder("Foglalt helyek: ");
        for (Seat seat : selectedSeats) {
            seatsText.append(seat.getSeatId()).append(" ");
        }
        Label seatsLabel = new Label(seatsText.toString());
        seatsLabel.setFont(Font.font(18));
        seatsLabel.setTextFill(Color.LIGHTGRAY);

        int totalPrice = selectedSeats.size() * movie.getPrice();
        Label priceLabel = new Label("Végösszeg: " + totalPrice + " Ft");
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        priceLabel.setTextFill(Color.web("#FFC107"));

        movieCard.getChildren().addAll(imageView, movieLabel, seatsLabel, priceLabel);

        // Gombok
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button newBookingButton = new Button("Új foglalás");
        newBookingButton.getStyleClass().add("select-button");
        newBookingButton.setOnAction(e -> mainApp.showMovieSelectionScene());

        Button exitButton = new Button("Kilépés");
        exitButton.getStyleClass().add("select-button");
        exitButton.setOnAction(e -> System.exit(0));

        buttonBox.getChildren().addAll(newBookingButton, exitButton);

        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(titleLabel, thankYouLabel, movieCard, buttonBox);
        root.getStyleClass().add("root");
    }

    private Image createPlaceholderImage() {
        try {
            return new Image(getClass().getResourceAsStream("/images/placeholder.jpg"));
        } catch (Exception e) {
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        }
    }
}