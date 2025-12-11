package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;

public class MovieSelectionController {
    private static final Logger LOGGER = Logger.getLogger(MovieSelectionController.class.getName());

    @FXML private FlowPane moviesFlowPane;
    @FXML private Button addMovieButton;
    @FXML private TextField searchField;
    @FXML private DatePicker filterDatePicker;

    private DatabaseManager dbManager;
    private MainApp mainApp;


    private boolean adminMode = false;

    private List<Movie> allMovies = new ArrayList<>();

    @FXML
    public void initialize() {
        dbManager = new DatabaseManager();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });


        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
        loadMovies();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }





    @FXML
    private void handleAdminLogin() {
        if (adminMode) {
            // Ha már admin, akkor kijelentkezés
            adminMode = false;
            addMovieButton.setVisible(false);
            addMovieButton.setManaged(false);
            mainApp.showInfo("Admin", "Sikeres kijelentkezés.");
        } else {

            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Admin Belépés");
            dialog.setHeaderText("Kérjük, adja meg az admin jelszót:");
            dialog.setContentText("Jelszó:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get().equals("admin123")) { // ITT A JELSZÓ (ezt írd át amire akarod)
                    adminMode = true;
                    addMovieButton.setVisible(true);
                    addMovieButton.setManaged(true);
                    mainApp.showInfo("Admin", "Sikeres bejelentkezés! Most már szerkeszthet.");
                } else {
                    mainApp.showError("Hiba", "Hibás jelszó!");
                }
            }
        }

        loadMovies();
    }

    @FXML
    private void handleRefresh() {
        loadMovies();
    }

    @FXML
    private void handleAddNewMovie(ActionEvent actionEvent) {
        System.out.println("DEBUG: Az 'Új film hozzáadása' gombra kattintottak.");
        if (mainApp != null) {
            System.out.println("DEBUG: mainApp létezik, hívjuk a showAddMovieScene() metódust.");
            mainApp.showAddMovieScene();
        } else {
            LOGGER.warning("mainApp nincs beállítva!");
        }
    }

    private void handleDeleteMovie(Movie movie) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Törlés megerősítése");
        alert.setHeaderText("Biztosan törölni szeretné ezt a vetítést?");
        alert.setContentText(movie.getTitle() + " (" + movie.getShowtime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")) + ")");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            boolean success = dbManager.deleteShowtime(movie.getVetitesId());
            if (success) {
                loadMovies(); // Lista frissítése
                mainApp.showInfo("Siker", "A vetítés törölve.");
            } else {
                mainApp.showError("Hiba", "Nem sikerült törölni a vetítést.");
            }
        }
    }



    private void loadMovies() {
        allMovies = dbManager.getAllMovies();
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        LocalDate selectedDate = filterDatePicker.getValue();

        List<Movie> filteredList = allMovies.stream()
                .filter(movie -> {

                    boolean matchesTitle = true;
                    if (!searchText.isEmpty()) {
                        matchesTitle = movie.getTitle().toLowerCase().contains(searchText);
                    }


                    boolean matchesDate = true;
                    if (selectedDate != null) {
                        // A LocalDateTime-ból kivesszük a LocalDate részt az összehasonlításhoz
                        matchesDate = movie.getShowtime().toLocalDate().equals(selectedDate);
                    }

                    return matchesTitle && matchesDate;
                })
                .collect(Collectors.toList());

        displayMovies(filteredList);
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

        ImageView imageView = new ImageView();
        try {
            Image image = new Image(movie.getImageUrl(), true);
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Nem sikerült betölteni a képet a filmhez: " + movie.getTitle(), e);
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
            imageView.setStyle("-fx-background-color: #444;");
        }

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-title");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(movie.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        descLabel.setMaxHeight(60);

        descLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");
        Label showtimeLabel = new Label(movie.getShowtime().format(formatter));
        showtimeLabel.setStyle("-fx-text-fill: #e0e0e0;");

        Label priceLabel = new Label(movie.getPrice() + " Ft");
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");


        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);


        Button selectButton = new Button("Kiválasztás");
        selectButton.getStyleClass().add("select-button");
        selectButton.setOnAction(e -> {
            if (mainApp != null) {
                // Most már a Részletek oldalra navigálunk!
                mainApp.showMovieDetailsScene(movie);
            } else {
                LOGGER.warning("mainApp nincs beállítva!");
            }
        });
        buttonBox.getChildren().add(selectButton);


        if (adminMode) {
            Button deleteButton = new Button("Törlés");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> handleDeleteMovie(movie));
            buttonBox.getChildren().add(deleteButton);
        }

        card.getChildren().addAll(imageView, titleLabel, descLabel, showtimeLabel, priceLabel, buttonBox);
        return card;
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterDatePicker.setValue(null);
    }
}