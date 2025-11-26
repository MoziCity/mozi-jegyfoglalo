package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;

public class MovieDetailsController {

    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label priceLabel;

    private MainApp mainApp;
    private Movie movie;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        updateUI();
    }

    private void updateUI() {
        if (movie != null) {
            titleLabel.setText(movie.getTitle());
            descriptionLabel.setText(movie.getDescription());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");
            showtimeLabel.setText(movie.getShowtime().format(formatter));

            priceLabel.setText(movie.getPrice() + " Ft");

            // Kép betöltése
            try {
                Image image = new Image(movie.getImageUrl(), true);
                posterImageView.setImage(image);
            } catch (Exception e) {
                // Placeholder, ha nincs kép
                posterImageView.setStyle("-fx-effect: null;"); // Effektek levétele hiba esetén
            }
        }
    }

    @FXML
    private void handleBack() {
        mainApp.showMovieSelectionScene();
    }

    @FXML
    private void handleBookTicket() {
        mainApp.showSeatSelectionScene(movie);
    }
}