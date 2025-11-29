package com.MoziCity.mozi_jegyfoglalo.db;

import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import org.springframework.stereotype.Repository; // Spring Boot-hoz kell

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository // Ez jelzi a Spring-nek, hogy ez egy adatbázis-kezelő osztály
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:mozi.db";
    private final DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- CSATLAKOZÁS ---
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // --- ADATBÁZIS LÉTREHOZÁSA (SETUP) ---
    public void setupDatabase() {
        String createFilmekTable = "CREATE TABLE IF NOT EXISTS Filmek (id INTEGER PRIMARY KEY AUTOINCREMENT, cim TEXT NOT NULL, leiras TEXT, imageUrl TEXT);";
        String createVetitesekTable = "CREATE TABLE IF NOT EXISTS Vetitesek (id INTEGER PRIMARY KEY AUTOINCREMENT, film_id INTEGER NOT NULL, datum TEXT NOT NULL, idopont TEXT NOT NULL, jegyar REAL NOT NULL, FOREIGN KEY(film_id) REFERENCES Filmek(id));";
        String createFelhasznalokTable = "CREATE TABLE IF NOT EXISTS Felhasznalok (id INTEGER PRIMARY KEY AUTOINCREMENT, nev TEXT NOT NULL, email TEXT NOT NULL UNIQUE);";
        String createHelyekTable = "CREATE TABLE IF NOT EXISTS Helyek (id INTEGER PRIMARY KEY AUTOINCREMENT, vetites_id INTEGER NOT NULL, sor TEXT NOT NULL, szam INTEGER NOT NULL, statusz TEXT NOT NULL DEFAULT 'FREE', FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id));";
        String createFoglalasokTable = "CREATE TABLE IF NOT EXISTS Foglalasok (id INTEGER PRIMARY KEY AUTOINCREMENT, vetites_id INTEGER NOT NULL, felhasznalo_id INTEGER NOT NULL, ules TEXT NOT NULL, ar REAL NOT NULL, foglalas_datuma TEXT NOT NULL, FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id), FOREIGN KEY(felhasznalo_id) REFERENCES Felhasznalok(id));";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createFilmekTable);
            stmt.execute(createVetitesekTable);
            stmt.execute(createFelhasznalokTable);
            stmt.execute(createHelyekTable);
            stmt.execute(createFoglalasokTable);

            // Ha üres az adatbázis, töltsünk be dummy adatokat
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Filmek");
            if (rs.getInt(1) == 0) {
                System.out.println("Adatbázis inicializálása dummy adatokkal...");
                populateDummyData(conn);
            }

        } catch (SQLException e) {
            System.err.println("Hiba az adatbázis setup közben: " + e.getMessage());
        }
    }

    // --- LEKÉRDEZÉSEK ---

    public List<Movie> getAllMovies() {
        String sql = "SELECT v.id as vetites_id, f.cim, f.leiras, f.imageUrl, v.datum, v.idopont, v.jegyar " +
                "FROM Filmek f JOIN Vetitesek v ON f.id = v.film_id";
        List<Movie> movies = new ArrayList<>();
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int vetitesId = rs.getInt("vetites_id");
                String title = rs.getString("cim");
                String description = rs.getString("leiras");
                String imageUrl = rs.getString("imageUrl");
                String dateStr = rs.getString("datum");
                String timeStr = rs.getString("idopont");
                int price = (int) rs.getDouble("jegyar");
                LocalDateTime showtime = LocalDateTime.parse(dateStr + " " + timeStr, parseFormatter);
                movies.add(new Movie(vetitesId, title, imageUrl, description, showtime, price));
            }
        } catch (SQLException e) {
            System.err.println("Hiba a filmek lekérdezésekor: " + e.getMessage());
        }
        return movies;
    }

    public List<Seat> getSeatsForShow(int vetitesId) {
        String sql = "SELECT sor, szam, statusz FROM Helyek WHERE vetites_id = ?";
        List<Seat> seats = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vetitesId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String row = rs.getString("sor");
                int number = rs.getInt("szam");
                SeatStatus status = SeatStatus.valueOf(rs.getString("statusz"));
                seats.add(new Seat(row, number, status));
            }
        } catch (SQLException e) {
            System.err.println("Hiba a helyek lekérdezésekor: " + e.getMessage());
        }
        return seats;
    }

    // --- FOGLALÁS MENTÉSE (Bővített verzió) ---
    public boolean saveBooking(int vetitesId, List<Seat> selectedSeats, int totalPrice, String customerName, String customerEmail) {
        String insertBookingSql = "INSERT INTO Foglalasok(vetites_id, felhasznalo_id, ules, ar, foglalas_datuma) VALUES(?,?,?,?,?)";
        String updateSeatsSql = "UPDATE Helyek SET statusz = 'TAKEN' WHERE vetites_id = ? AND sor = ? AND szam = ?";

        Connection conn = null; // Kívül deklaráljuk a hibakezelés miatt
        try {
            conn = this.connect();
            if (conn == null) throw new SQLException("Sikertelen kapcsolódás.");

            conn.setAutoCommit(false); // Tranzakció indítása

            // 1. Felhasználó ID lekérése vagy létrehozása
            int felhasznaloId = getOrCreateUser(conn, customerName, customerEmail);

            // 2. Foglalás beszúrása
            try (PreparedStatement pstmtBooking = conn.prepareStatement(insertBookingSql)) {
                String seatsStr = selectedSeats.stream()
                        .map(Seat::getSeatId)
                        .collect(Collectors.joining(", "));

                pstmtBooking.setInt(1, vetitesId);
                pstmtBooking.setInt(2, felhasznaloId);
                pstmtBooking.setString(3, seatsStr);
                pstmtBooking.setDouble(4, totalPrice);
                pstmtBooking.setString(5, LocalDateTime.now().format(dbFormatter));
                pstmtBooking.executeUpdate();
            }

            // 3. Helyek frissítése
            try (PreparedStatement pstmtUpdateSeats = conn.prepareStatement(updateSeatsSql)) {
                for (Seat seat : selectedSeats) {
                    pstmtUpdateSeats.setInt(1, vetitesId);
                    pstmtUpdateSeats.setString(2, seat.getRow());
                    pstmtUpdateSeats.setInt(3, seat.getNumber());
                    pstmtUpdateSeats.addBatch();
                }
                pstmtUpdateSeats.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Hiba a foglalás mentésekor: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- ADMIN FUNKCIÓK ---

    // Új film hozzáadása
    public boolean addNewMovieAndShowtime(String title, String description, String imageUrl,
                                          String date, String time, double price) {

        String insertFilmSql = "INSERT INTO Filmek (cim, leiras, imageUrl) VALUES (?,?,?)";
        String insertVetitesSql = "INSERT INTO Vetitesek (film_id, datum, idopont, jegyar) VALUES (?,?,?,?)";
        String insertHelyekSql = "INSERT INTO Helyek (vetites_id, sor, szam, statusz) VALUES (?,?,?,?)";

        Connection conn = null;
        try {
            conn = this.connect();
            conn.setAutoCommit(false);

            int filmId;
            int vetitesId;

            // 1. Film
            try (PreparedStatement pstmtFilm = conn.prepareStatement(insertFilmSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtFilm.setString(1, title);
                pstmtFilm.setString(2, description);
                pstmtFilm.setString(3, imageUrl);
                pstmtFilm.executeUpdate();
                filmId = getGeneratedId(pstmtFilm);
            }

            // 2. Vetítés
            try (PreparedStatement pstmtVetites = conn.prepareStatement(insertVetitesSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVetites.setInt(1, filmId);
                pstmtVetites.setString(2, date);
                pstmtVetites.setString(3, time);
                pstmtVetites.setDouble(4, price);
                pstmtVetites.executeUpdate();
                vetitesId = getGeneratedId(pstmtVetites);
            }

            // 3. Helyek
            try (PreparedStatement pstmtHely = conn.prepareStatement(insertHelyekSql)) {
                generateSeatsForShow(pstmtHely, vetitesId);
            }

            conn.commit();
            System.out.println("Új film és vetítés sikeresen mentve.");
            return true;

        } catch (SQLException e) {
            System.err.println("Hiba az új film mentésekor: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Vetítés törlése
    public boolean deleteShowtime(int vetitesId) {
        String deleteFoglalasok = "DELETE FROM Foglalasok WHERE vetites_id = ?";
        String deleteHelyek = "DELETE FROM Helyek WHERE vetites_id = ?";
        String deleteVetites = "DELETE FROM Vetitesek WHERE id = ?";

        Connection conn = null;
        try {
            conn = this.connect();
            conn.setAutoCommit(false);

            // 1. Foglalások törlése
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFoglalasok)) {
                pstmt.setInt(1, vetitesId);
                pstmt.executeUpdate();
            }

            // 2. Helyek törlése
            try (PreparedStatement pstmt = conn.prepareStatement(deleteHelyek)) {
                pstmt.setInt(1, vetitesId);
                pstmt.executeUpdate();
            }

            // 3. Maga a vetítés törlése
            try (PreparedStatement pstmt = conn.prepareStatement(deleteVetites)) {
                pstmt.setInt(1, vetitesId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Nem található a törlendő vetítés.");
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Hiba a törlés során: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- SEGÉDMETÓDUSOK (Privát) ---

    private void populateDummyData(Connection conn) throws SQLException {
        try (PreparedStatement pstmtFilm = conn.prepareStatement("INSERT INTO Filmek (cim, leiras, imageUrl) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtVetites = conn.prepareStatement("INSERT INTO Vetitesek (film_id, datum, idopont, jegyar) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtHely = conn.prepareStatement("INSERT INTO Helyek (vetites_id, sor, szam, statusz) VALUES (?,?,?,?)")) {

            conn.setAutoCommit(false);

            // Borat
            pstmtFilm.setString(1, "Borat");
            pstmtFilm.setString(2, "A Borat: Kazah nép nagy fehér gyermeke Amerikába megy.");
            pstmtFilm.setString(3, "/images/film1.jpg");
            pstmtFilm.executeUpdate();
            int boratFilmId = getGeneratedId(pstmtFilm);

            pstmtVetites.setInt(1, boratFilmId);
            pstmtVetites.setString(2, "2025-11-10");
            pstmtVetites.setString(3, "18:00");
            pstmtVetites.setDouble(4, 2500);
            pstmtVetites.executeUpdate();
            int boratVetitesId = getGeneratedId(pstmtVetites);
            generateSeatsForShow(pstmtHely, boratVetitesId);

            // K-pop
            pstmtFilm.setString(1, "K-pop démonvadászok");
            pstmtFilm.setString(2, "A K-pop démonvadászok 2025-ben bemutatott amerikai animációs film.");
            pstmtFilm.setString(3, "/images/film2.jpg");
            pstmtFilm.executeUpdate();
            int kpopFilmId = getGeneratedId(pstmtFilm);

            pstmtVetites.setInt(1, kpopFilmId);
            pstmtVetites.setString(2, "2025-11-10");
            pstmtVetites.setString(3, "20:30");
            pstmtVetites.setDouble(4, 2200);
            pstmtVetites.executeUpdate();
            int kpopVetitesId = getGeneratedId(pstmtVetites);
            generateSeatsForShow(pstmtHely, kpopVetitesId);

            // Barbie
            pstmtFilm.setString(1, "Barbie");
            pstmtFilm.setString(2, "A Barbie 2023-ban bemutatott amerikai fantasy filmvígjáték.");
            pstmtFilm.setString(3, "/images/film3.jpg");
            pstmtFilm.executeUpdate();
            int barbieFilmId = getGeneratedId(pstmtFilm);

            pstmtVetites.setInt(1, barbieFilmId);
            pstmtVetites.setString(2, "2025-11-11");
            pstmtVetites.setString(3, "19:00");
            pstmtVetites.setDouble(4, 2400);
            pstmtVetites.executeUpdate();
            int barbieVetitesId = getGeneratedId(pstmtVetites);
            generateSeatsForShow(pstmtHely, barbieVetitesId);

            conn.commit();
            System.out.println("Dummy adatok sikeresen betöltve.");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Nem sikerült ID-t generálni.");
            }
        }
    }

    private void generateSeatsForShow(PreparedStatement pstmtHely, int vetitesId) throws SQLException {
        for (char row = 'A'; row <= 'H'; row++) {
            for (int number = 1; number <= 10; number++) {
                pstmtHely.setInt(1, vetitesId);
                pstmtHely.setString(2, String.valueOf(row));
                pstmtHely.setInt(3, number);
                pstmtHely.setString(4, SeatStatus.FREE.name());
                pstmtHely.addBatch();
            }
        }
        pstmtHely.executeBatch();
    }

    private int getOrCreateUser(Connection conn, String name, String email) throws SQLException {
        String selectSql = "SELECT id FROM Felhasznalok WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        String insertSql = "INSERT INTO Felhasznalok(nev, email) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Nem sikerült létrehozni a felhasználót.");
                }
            }
        }
    }
}