package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovieSelectionController {
    @FXML
    private FlowPane moviesFlowPane;

    private MainApp mainApp;
    private List<Movie> movies;

    @FXML
    public void initialize() {
        // Inicializálás - ide jönnek a filmek betöltése
        loadMovies();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void loadMovies() {
        // Ide jönne a filmek betöltése adatbázisból vagy szolgáltatásból
        // Most csak példa adatokkal dolgozunk
        movies = List.of(
                new Movie("Borat", "/images/film1.jpg", "A Borat: Kazah nép nagy fehér gyermeke Amerikába megy.",
                        java.time.LocalDateTime.now().plusDays(1).withHour(18).withMinute(0), 2500),
                new Movie("K-pop démonvadászok", "/images/film2.jpg", "A K-pop démonvadászok 2025-ben bemutatott amerikai animációs film.",
                        java.time.LocalDateTime.now().plusDays(1).withHour(20).withMinute(30), 2200),
                new Movie("Barbie", "/images/film3.jpg", "A Barbie 2023-ban bemutatott amerikai fantasy filmvígjáték.",
                        java.time.LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), 2400)
        );

        displayMovies();
    }

    private void displayMovies() {
        moviesFlowPane.getChildren().clear();

        for (Movie movie : movies) {
            VBox movieCard = createMovieCard(movie);
            moviesFlowPane.getChildren().add(movieCard);
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");

        // Kép (placeholder, ha nincs URL)
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(movie.getImageUrl(), true);
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            // Ha a kép nem elérhető, használjunk placeholder-t
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
            imageView.setStyle("-fx-background-color: #444;");
        }

        // Cím
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        // Időpont
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");
        Label showtimeLabel = new Label(movie.getShowtime().format(formatter));
        showtimeLabel.setStyle("-fx-text-fill: #e0e0e0;");

        // Ár
        Label priceLabel = new Label(movie.getPrice() + " Ft");
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        // Kiválasztás gomb
        Button selectButton = new Button("Kiválasztás");
        selectButton.getStyleClass().add("select-button");
        selectButton.setOnAction(e -> mainApp.showSeatSelectionScene(movie));

        card.getChildren().addAll(imageView, titleLabel, showtimeLabel, priceLabel, selectButton);

        return card;
    }

    @FXML
    private void handleRefresh() {
        loadMovies();
    }
}