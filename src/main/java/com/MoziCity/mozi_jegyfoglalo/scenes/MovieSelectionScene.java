package com.MoziCity.mozi_jegyfoglalo.scenes;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class MovieSelectionScene extends Scene {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 700;

    public MovieSelectionScene(MainApp mainApp) {
        super(new VBox(), WIDTH, HEIGHT);
        VBox root = (VBox) getRoot();

        // Stíluslap alkalmazása
        getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Cím
        Label titleLabel = new Label("Válasszon filmet");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        // Filmek listája
        FlowPane moviePane = new FlowPane();
        moviePane.setHgap(20);
        moviePane.setVgap(20);
        moviePane.setPadding(new Insets(20));
        moviePane.setAlignment(Pos.CENTER);

        // Dummy adatok
        List<Movie> movies = createDummyMovies();

        for (Movie movie : movies) {
            VBox movieCard = createMovieCard(movie, mainApp);
            moviePane.getChildren().add(movieCard);
        }

        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.getChildren().addAll(titleLabel, moviePane);
        root.getStyleClass().add("root");
    }

    private VBox createMovieCard(Movie movie, MainApp mainApp) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("movie-card");

        // Kép betöltése hibakezeléssel
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(false);

        try {
            // Próbáljuk meg betölteni a képet
            Image image = new Image(movie.getImageUrl(), true);
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Ha hiba történt, használjunk placeholder-t
                    imageView.setImage(createPlaceholderImage());
                }
            });
            imageView.setImage(image);
        } catch (Exception e) {
            // Ha a kép nem elérhető, használjunk placeholder-t
            imageView.setImage(createPlaceholderImage());
        }

        Label title = new Label(movie.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        Label description = new Label(movie.getDescription());
        description.setFont(Font.font(14));
        description.setTextFill(Color.LIGHTGRAY);
        description.setWrapText(true);
        description.setMaxWidth(200);

        Button selectButton = new Button("Jegyfoglalás");
        selectButton.getStyleClass().add("select-button");
        selectButton.setOnAction(e -> mainApp.showSeatSelectionScene(movie));

        card.getChildren().addAll(imageView, title, description, selectButton);
        return card;
    }

    private Image createPlaceholderImage() {
        // Egyszerű placeholder kép létrehozása programozottan
        // Vagy használhatnánk egy alapértelmezett képet a resources mappából
        try {
            return new Image(getClass().getResourceAsStream("/images/placeholder.jpg"));
        } catch (Exception e) {
            // Ha a placeholder kép sincs, akkor egy üres képet adunk vissza
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        }
    }

    private List<Movie> createDummyMovies() {
        List<Movie> movies = new ArrayList<>();
        // Használjunk online képeket vagy helyi placeholder-t
        movies.add(new Movie("Borat", "/images/film1.jpg", "Egy fiatal nemes útja egy veszélyes bolygóra, hogy megvédje családját és népét.", java.time.LocalDateTime.now().plusDays(1).withHour(19), 3500));
        movies.add(new Movie("KPOP Demon Hunters", "/images/film2.jpg", "Az atombomba atyjának története, aki megváltoztatta a világot.", java.time.LocalDateTime.now().plusDays(1).withHour(21), 3500));
        movies.add(new Movie("Barbie", "/images/film3.png", "Barbie kalandjai a való világban, ahol rájön, hogy a tökéletesség nem minden.", java.time.LocalDateTime.now().plusDays(2).withHour(18), 3200));
        return movies;
    }
}