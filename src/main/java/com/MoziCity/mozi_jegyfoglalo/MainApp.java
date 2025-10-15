package com.MoziCity.mozi_jegyfoglalo;

import com.MoziCity.mozi_jegyfoglalo.controller.*;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {

    private Stage primaryStage;
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
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/movie-selection.fxml"));
            Parent root = loader.load();

            MovieSelectionController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showError("Hiba", "Nem sikerült betölteni a filmválasztó képernyőt:\n" + e.getMessage());
        }
    }

    public void showSeatSelectionScene(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/seat-selection.fxml"));
            Parent root = loader.load();

            SeatSelectionController controller = loader.getController();
            controller.setMainApp(this);
            controller.setMovie(movie);

            Scene scene = new Scene(root);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showError("Hiba", "Nem sikerült betölteni a helyválasztó képernyőt:\n" + e.getMessage());
        }
    }

    public void showConfirmationScene(Movie movie, List<Seat> selectedSeats) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/confirmation.fxml"));
            Parent root = loader.load();

            ConfirmationController controller = loader.getController();
            controller.setMainApp(this);
            controller.setBookingDetails(movie, selectedSeats);

            Scene scene = new Scene(root);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showError("Hiba", "Nem sikerült betölteni a megerősítő képernyőt:\n" + e.getMessage());
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            String stylesheet = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } catch (NullPointerException e) {
            System.err.println("Figyelem: Nem található a stíluslap: /style.css");
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