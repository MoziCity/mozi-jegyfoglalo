package com.MoziCity.mozi_jegyfoglalo;

import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.scenes.ConfirmationScene;
import com.MoziCity.mozi_jegyfoglalo.scenes.MovieSelectionScene;
import com.MoziCity.mozi_jegyfoglalo.scenes.SeatSelectionScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Cinema Mozi Jegyfoglaló");
        this.primaryStage.setResizable(false);

        showMovieSelectionScene();
        primaryStage.show();
    }

    public void showMovieSelectionScene() {
        MovieSelectionScene scene = new MovieSelectionScene(this);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public void showSeatSelectionScene(Movie movie) {
        SeatSelectionScene scene = new SeatSelectionScene(movie, this);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public void showConfirmationScene(Movie movie, List<Seat> selectedSeats) {
        ConfirmationScene scene = new ConfirmationScene(movie, selectedSeats, this);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}