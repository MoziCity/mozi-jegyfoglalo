package com.MoziCity.mozi_jegyfoglalo;

import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.scenes.ConfirmationScene;
import com.MoziCity.mozi_jegyfoglalo.scenes.MovieSelectionScene;
import com.MoziCity.mozi_jegyfoglalo.scenes.SeatSelectionScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;


public class MainApp extends Application {

    private Stage primaryStage;
    private static final String STYLESHEET_PATH = "/style.css";
    private static final String APP_TITLE = "MoziCity Jegyfoglaló";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Fő ablak beállítása
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        // Bezárás kezelése megerősítéssel
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            handleCloseRequest();
        });

        // Kezdeti jelenet megjelenítése
        showMovieSelectionScene();
        primaryStage.show();
    }

    public void showMovieSelectionScene() {
        try {
            MovieSelectionScene scene = new MovieSelectionScene(this);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            showError("Hiba", "Nem sikerült betölteni a filmválasztó képernyőt:\n" + e.getMessage());
        }
    }

    public void showSeatSelectionScene(Movie movie) {
        try {
            SeatSelectionScene scene = new SeatSelectionScene(movie, this);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            showError("Hiba", "Nem sikerült betölteni a helyválasztó képernyőt:\n" + e.getMessage());
        }
    }

    public void showConfirmationScene(Movie movie, List<Seat> selectedSeats) {
        try {
            ConfirmationScene scene = new ConfirmationScene(movie, selectedSeats, this);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            showError("Hiba", "Nem sikerült betölteni a megerősítő képernyőt:\n" + e.getMessage());
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            String stylesheet = getClass().getResource(STYLESHEET_PATH).toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } catch (NullPointerException e) {
            System.err.println("Figyelem: Nem található a stíluslap: " + STYLESHEET_PATH);
            System.err.println("Az alkalmazás tovább fut egyedi stílusok nélkül.");
        }
    }

    private void handleCloseRequest() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kilépés");
        alert.setHeaderText("Biztosan ki szeretne lépni?");
        alert.setContentText("Minden nem mentett változás elvész.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hiba");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Információ");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Megerősítés");
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}