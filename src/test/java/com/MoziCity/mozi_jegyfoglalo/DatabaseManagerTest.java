package com.MoziCity.mozi_jegyfoglalo;

import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseManagerTest {

    private static DatabaseManager dbManager;
    private static Movie testMovie;

    @BeforeAll
    static void setup() {

        dbManager = new DatabaseManager();
        dbManager.setupDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("Új film és vetítés hozzáadása teszt")
    void testAddNewMovie() {

        boolean result = dbManager.addNewMovieAndShowtime(
                "TESZT FILM",
                "Ez egy teszt leírás",
                "https://example.com/poster.jpg",
                "2030-01-01",
                "12:00",
                3000.0
        );

        assertTrue(result, "A film mentésének sikeresnek kell lennie.");


        List<Movie> movies = dbManager.getAllMovies();
        testMovie = movies.stream()
                .filter(m -> m.getTitle().equals("TESZT FILM"))
                .findFirst()
                .orElse(null);

        assertNotNull(testMovie, "A teszt filmnek szerepelnie kell a listában.");
        assertEquals(3000, testMovie.getPrice(), "Az árnak egyeznie kell.");
    }

    @Test
    @Order(2)
    @DisplayName("Helyek generálásának ellenőrzése")
    void testSeatGeneration() {
        assertNotNull(testMovie, "A teszt filmnek léteznie kell az előző tesztből.");


        List<Seat> seats = dbManager.getSeatsForShow(testMovie.getVetitesId());


        assertFalse(seats.isEmpty(), "A helyek listája nem lehet üres.");


        boolean allFree = seats.stream().allMatch(s -> s.getStatus() == SeatStatus.FREE);
        assertTrue(allFree, "Minden helynek kezdetben szabadnak kell lennie.");
    }

    @Test
    @Order(3)
    @DisplayName("Foglalás mentésének tesztelése")
    void testBooking() {
        assertNotNull(testMovie, "A teszt filmnek léteznie kell.");


        List<Seat> seatsToBook = new ArrayList<>();
        seatsToBook.add(new Seat("A", 1, SeatStatus.SELECTED));
        seatsToBook.add(new Seat("A", 2, SeatStatus.SELECTED));

        int totalPrice = testMovie.getPrice() * seatsToBook.size();


        boolean success = dbManager.saveBooking(
                testMovie.getVetitesId(),
                seatsToBook,
                totalPrice,
                "Teszt Elek",
                "teszt@email.hu"
        );

        assertTrue(success, "A foglalás mentésének sikeresnek kell lennie.");


        List<Seat> updatedSeats = dbManager.getSeatsForShow(testMovie.getVetitesId());

        Seat bookedSeat1 = updatedSeats.stream()
                .filter(s -> s.getRow().equals("A") && s.getNumber() == 1)
                .findFirst().orElse(null);

        assertNotNull(bookedSeat1);
        assertEquals(SeatStatus.TAKEN, bookedSeat1.getStatus(), "Az A1 széknek foglaltnak kell lennie.");
    }

    @Test
    @Order(4)
    @DisplayName("Vetítés törlésének tesztelése (Takarítás)")
    void testDeleteMovie() {
        assertNotNull(testMovie, "A teszt filmnek léteznie kell.");

        boolean deleted = dbManager.deleteShowtime(testMovie.getVetitesId());
        assertTrue(deleted, "A törlésnek sikeresnek kell lennie.");


        List<Movie> movies = dbManager.getAllMovies();
        boolean exists = movies.stream().anyMatch(m -> m.getVetitesId() == testMovie.getVetitesId());

        assertFalse(exists, "A filmnek már nem szabad léteznie az adatbázisban.");
    }
}