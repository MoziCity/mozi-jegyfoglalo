package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieSelectionController {
    private static final Logger LOGGER = Logger.getLogger(MovieSelectionController.class.getName());

    @FXML
    private FlowPane moviesFlowPane;
    private DatabaseManager dbManager;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        // Inicializálás - ide jönnek a filmek betöltése
        dbManager = new DatabaseManager();
        loadMovies();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void loadMovies() {
        List<Movie> movies = dbManager.getAllMovies();
        displayMovies(movies);
    }

    private void displayMovies(List<Movie> movies) {
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
            LOGGER.log(Level.WARNING, "Nem sikerült betölteni a képet a filmhez: " + movie.getTitle(), e);
            // Ha a kép nem elérhető, használjunk placeholder-t
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
            imageView.setStyle("-fx-background-color: #444;");
        }

        // Cím
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-title");

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
        selectButton.setOnAction(e -> {
            if (mainApp != null) {
                mainApp.showSeatSelectionScene(movie);
            } else {
                LOGGER.warning("mainApp nincs beállítva!");
            }
        });

        card.getChildren().addAll(imageView, titleLabel, showtimeLabel, priceLabel, selectButton);

        return card;
    }
    @FXML
    private void handleRefresh() {
        loadMovies();
    }

    @FXML
    private void handleAddNewMovie() {
        if (mainApp != null) {
            mainApp.showAddMovieScene();
        } else {
            LOGGER.warning("A mainApp referencia null, nem lehet navigálni!");
        }
    }



}