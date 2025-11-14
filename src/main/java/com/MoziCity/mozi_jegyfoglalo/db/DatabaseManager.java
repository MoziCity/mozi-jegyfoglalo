package com.MoziCity.mozi_jegyfoglalo.db;

import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {



    private static final String DB_URL = "jdbc:sqlite:mozi.db";

    // Formátum a LocalDateTime tárolásához az adatbázisban (TEXT-ként)
    private final DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Csatlakozik az SQLite adatbázishoz.
     * @return Connection objektum
     */
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Hiba a csatlakozáskor: " + e.getMessage());
        }
        return conn;
    }

    // ... (a DatabaseManager osztályon belül) ...

    /**
     * Létrehozza az adatbázis sémát (táblákat), ha azok még nem léteznek,
     * és feltölti őket alapértelmezett adatokkal.
     */
    public void setupDatabase() {
        // A 'Filmek' tábla a PDF [cite: 112] és a Movie modell alapján
        String createFilmekTable = "CREATE TABLE IF NOT EXISTS Filmek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cim TEXT NOT NULL," +
                "leiras TEXT," +
                "imageUrl TEXT" + // Ez a te Movie modelledhez kell
                ");";

        // A 'Vetitesek' tábla a PDF [cite: 113] és a Movie modell alapján
        String createVetitesekTable = "CREATE TABLE IF NOT EXISTS Vetitesek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "film_id INTEGER NOT NULL," +
                "datum TEXT NOT NULL," + // Pl. "2025-11-20"
                "idopont TEXT NOT NULL," + // Pl. "18:00"
                "jegyar REAL NOT NULL," + // Az ár a vetítéshez tartozik
                "FOREIGN KEY(film_id) REFERENCES Filmek(id)" +
                ");";

        // A 'Helyek' tábla (ezt mi találtuk ki, de logikailag szükséges)
        // Tárolja egy adott vetítés összes helyét és annak állapotát
        String createHelyekTable = "CREATE TABLE IF NOT EXISTS Helyek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "vetites_id INTEGER NOT NULL," +
                "sor TEXT NOT NULL," + // 'A', 'B', 'C' ...
                "szam INTEGER NOT NULL," + // 1, 2, 3 ...
                "statusz TEXT NOT NULL DEFAULT 'FREE'," + // FREE, TAKEN
                "FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id)" +
                ");";

        // A 'Foglalasok' tábla a PDF alapján [cite: 114, 119]
        String createFoglalasokTable = "CREATE TABLE IF NOT EXISTS Foglalasok (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "vetites_id INTEGER NOT NULL," +
                "felhasznalo_id INTEGER," + // Ezt most nem használjuk, de a sémában benne van
                "ules TEXT NOT NULL," + // Pl. "A1, A2, A3"
                "ar REAL NOT NULL," +
                "foglalas_datuma TEXT NOT NULL," +
                "FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id)" +
                ");";

        try (Connection conn = this.connect();
             java.sql.Statement stmt = conn.createStatement()) {

            // Táblák létrehozása
            stmt.execute(createFilmekTable);
            stmt.execute(createVetitesekTable);
            stmt.execute(createHelyekTable);
            stmt.execute(createFoglalasokTable);

            // ----- ADATOK FELTÖLTÉSE (CSAK HA ÜRES) -----
            // Ellenőrizzük, vannak-e már filmek
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Filmek");
            if (rs.getInt(1) == 0) {
                // Nincsenek filmek, töltsük fel a dummy adatokkal
                System.out.println("Adatbázis inicializálása dummy adatokkal...");
                populateDummyData(conn);
            }

        } catch (SQLException e) {
            System.err.println("Hiba az adatbázis setup közben: " + e.getMessage());
        }
    }

    /**
     * Feltölti az adatbázist próba adatokkal.
     * Ezt a setupDatabase() hívja meg, ha az adatbázis üres.
     */
    private void populateDummyData(Connection conn) throws SQLException {
        // A te MovieSelectionController-ed dummy adatai

        // 1. Film: Borat
        // Használunk PreparedStatement-et az adatok beszúrásához
        try (PreparedStatement pstmtFilm = conn.prepareStatement("INSERT INTO Filmek (cim, leiras, imageUrl) VALUES (?,?,?)", new String[]{"id"});
             PreparedStatement pstmtVetites = conn.prepareStatement("INSERT INTO Vetitesek (film_id, datum, idopont, jegyar) VALUES (?,?,?,?)", new String[]{"id"});
             PreparedStatement pstmtHely = conn.prepareStatement("INSERT INTO Helyek (vetites_id, sor, szam, statusz) VALUES (?,?,?,?)")) {

            conn.setAutoCommit(false); // Tranzakció indítása

            // === Borat ===
            pstmtFilm.setString(1, "Borat");
            pstmtFilm.setString(2, "A Borat: Kazah nép nagy fehér gyermeke Amerikába megy.");
            pstmtFilm.setString(3, "/images/film1.jpg");
            pstmtFilm.executeUpdate();

            int boratFilmId = getGeneratedId(pstmtFilm); // Frissen beszúrt Film ID-ja

            // Vetítés a Borathoz
            pstmtVetites.setInt(1, boratFilmId);
            pstmtVetites.setString(2, "2025-11-10"); // Dátum
            pstmtVetites.setString(3, "18:00"); // Időpont
            pstmtVetites.setDouble(4, 2500); // Ár
            pstmtVetites.executeUpdate();

            int boratVetitesId = getGeneratedId(pstmtVetites); // Frissen beszúrt Vetítés ID-ja
            generateSeatsForShow(pstmtHely, boratVetitesId); // Helyek generálása ehhez a vetítéshez

            // === K-pop démonvadászok ===
            pstmtFilm.setString(1, "K-pop démonvadászok");
            pstmtFilm.setString(2, "A K-pop démonvadászok 2025-ben bemutatott amerikai animációs film.");
            pstmtFilm.setString(3, "/images/film2.jpg");
            pstmtFilm.executeUpdate();

            int kpopFilmId = getGeneratedId(pstmtFilm);

            // Vetítés a K-pophoz
            pstmtVetites.setInt(1, kpopFilmId);
            pstmtVetites.setString(2, "2025-11-10");
            pstmtVetites.setString(3, "20:30");
            pstmtVetites.setDouble(4, 2200);
            pstmtVetites.executeUpdate();

            int kpopVetitesId = getGeneratedId(pstmtVetites);
            generateSeatsForShow(pstmtHely, kpopVetitesId);

            // === Barbie ===
            pstmtFilm.setString(1, "Barbie");
            pstmtFilm.setString(2, "A Barbie 2023-ban bemutatott amerikai fantasy filmvígjáték.");
            pstmtFilm.setString(3, "/images/film3.jpg");
            pstmtFilm.executeUpdate();

            int barbieFilmId = getGeneratedId(pstmtFilm);

            // Vetítés a Barbie-hoz
            pstmtVetites.setInt(1, barbieFilmId);
            pstmtVetites.setString(2, "2025-11-11");
            pstmtVetites.setString(3, "19:00");
            pstmtVetites.setDouble(4, 2400);
            pstmtVetites.executeUpdate();

            int barbieVetitesId = getGeneratedId(pstmtVetites);
            generateSeatsForShow(pstmtHely, barbieVetitesId);

            conn.commit(); // Tranzakció véglegesítése
            System.out.println("Dummy adatok sikeresen betöltve.");

        } catch (SQLException e) {
            conn.rollback(); // Hiba esetén visszavonás
            System.err.println("Hiba a dummy adatok betöltésekor: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true); // Visszaállás normál módba
        }

    }
    public boolean addNewMovieAndShowtime(String title, String description, String imageUrl,
                                          String date, String time, double price) {

        String insertFilmSql = "INSERT INTO Filmek (cim, leiras, imageUrl) VALUES (?,?,?)";
        String insertVetitesSql = "INSERT INTO Vetitesek (film_id, datum, idopont, jegyar) VALUES (?,?,?,?)";
        String insertHelyekSql = "INSERT INTO Helyek (vetites_id, sor, szam, statusz) VALUES (?,?,?,?)";

        Connection conn = this.connect();
        try {
            conn.setAutoCommit(false); // Tranzakció indítása

            int filmId;
            int vetitesId;

            // 1. Film beszúrása
            try (PreparedStatement pstmtFilm = conn.prepareStatement(insertFilmSql, new String[]{"id"})) {
                pstmtFilm.setString(1, title);
                pstmtFilm.setString(2, description);
                pstmtFilm.setString(3, imageUrl);
                pstmtFilm.executeUpdate();
                filmId = getGeneratedId(pstmtFilm); // Segédmetódusunk használata
            }

            // 2. Vetítés beszúrása
            try (PreparedStatement pstmtVetites = conn.prepareStatement(insertVetitesSql, new String[]{"id"})) {
                pstmtVetites.setInt(1, filmId);
                pstmtVetites.setString(2, date);   // Formátum: "YYYY-MM-DD"
                pstmtVetites.setString(3, time);   // Formátum: "HH:mm"
                pstmtVetites.setDouble(4, price);
                pstmtVetites.executeUpdate();
                vetitesId = getGeneratedId(pstmtVetites);
            }

            // 3. Helyek generálása a vetítéshez
            try (PreparedStatement pstmtHely = conn.prepareStatement(insertHelyekSql)) {
                // Hívjuk a meglévő segédmetódusunkat, ami már tudja, hogy 'FREE' helyeket kell csinálni
                generateSeatsForShow(pstmtHely, vetitesId);
            }

            conn.commit(); // Tranzakció véglegesítése
            System.out.println("Új film és vetítés sikeresen mentve.");
            return true;

        } catch (SQLException e) {
            System.err.println("Hiba az új film mentésekor, rollback: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Hiba rollback közben: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Hiba a kapcsolat lezárásakor: " + e.getMessage());
            }
        }
    }

    /** Segédmetódus a frissen generált ID lekéréséhez (pl. AUTOINCREMENT) */
    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Nem sikerült ID-t generálni.");
            }
        }
    }

    /** Segédmetódus, legenerálja az ülőhelyeket egy adott vetítés ID-hoz */
    private void generateSeatsForShow(PreparedStatement pstmtHely, int vetitesId) throws SQLException {
        // 8 sor (A-H), 10 szék (1-10) [a SeatSelectionController-ed alapján]
        for (char row = 'A'; row <= 'H'; row++) {
            for (int number = 1; number <= 10; number++) {
                pstmtHely.setInt(1, vetitesId);
                pstmtHely.setString(2, String.valueOf(row));
                pstmtHely.setInt(3, number);

                pstmtHely.setString(4, SeatStatus.FREE.name());

                pstmtHely.addBatch(); // Gyorsabb beszúrás batch-csel
            }
        }
        pstmtHely.executeBatch(); // Batch végrehajtása
    }

    /**
     * Lekérdezi az összes filmet az adatbázisból.
     * Ez váltja majd ki a MovieSelectionController-ben lévő dummy listát.
     *
     * FONTOS: Ez a kód feltételezi, hogy van egy 'Vetitesek' tábla,
     * ami tartalmazza az időpontot és az árat, és össze van kötve a 'Filmek' táblával.
     * A PDF adatmodellje [cite: 83-112] alapján a 'Filmek' és 'Vetitesek' külön vannak.
     */
    public List<Movie> getAllMovies() {
        // A PDF-ben a 'Filmek' és 'Vetitesek' külön táblán vannak.
        // Most már lekérdezzük a vetítés ID-t is (v.id)!
        String sql = "SELECT v.id as vetites_id, f.cim, f.leiras, f.imageUrl, v.datum, v.idopont, v.jegyar " +
                "FROM Filmek f " +
                "JOIN Vetitesek v ON f.id = v.film_id";

        List<Movie> movies = new ArrayList<>();

        // Ez a formátum a "yyyy-MM-dd" és "HH:mm" stringek egyesítéséhez kell
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Adatok kiolvasása a ResultSet-ből
                int vetitesId = rs.getInt("vetites_id"); // [cite: 155]
                String title = rs.getString("cim"); // [cite: 156]
                String description = rs.getString("leiras");
                String imageUrl = rs.getString("imageUrl");
                String dateStr = rs.getString("datum");
                String timeStr = rs.getString("idopont");
                int price = (int) rs.getDouble("jegyar"); // [cite: 110]

                // Időpontok egyesítése LocalDateTime-má
                LocalDateTime showtime = LocalDateTime.parse(dateStr + " " + timeStr, parseFormatter);

                // Átadjuk az új Movie konstruktornak, immár a vetitesId-vel
                movies.add(new Movie(vetitesId, title, imageUrl, description, showtime, price));
            }
        } catch (SQLException e) {
            System.err.println("Hiba a filmek lekérdezésekor: " + e.getMessage());
        }
        return movies;
    }
    /**
     * Lekérdezi egy adott vetítés helyeit.
     * Ez váltja ki a SeatSelectionController-ben lévő dummy generátort.
     *
     * FONTOS: A PDF sémája [cite: 90] alapján a 'Foglalasok' tábla tárolja
     * a foglalt helyeket. A "szabad" helyeket a terem kiosztása adja.
     * Ez egy komplexebb lekérdezést igényelne (pl. összes hely - foglalt hely).
     *
     * Most leegyszerűsítjük: feltételezzük, hogy van egy 'Helyek' táblánk.
     */
    public List<Seat> getSeatsForShow(int vetitesId) {
        // Ez egy EGYSZERŰSÍTETT modell.
        // Egy 'Helyek' táblát feltételezünk, ami tárolja MINDEN hely státuszát
        // egy adott vetítésre (vetites_id, sor, szam, statusz)
        String sql = "SELECT sor, szam, statusz FROM Helyek WHERE vetites_id = ?";
        List<Seat> seats = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vetitesId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String row = rs.getString("sor");
                int number = rs.getInt("szam");
                SeatStatus status = SeatStatus.valueOf(rs.getString("statusz")); // FREE, TAKEN

                seats.add(new Seat(row, number, status));
            }
        } catch (SQLException e) {
            System.err.println("Hiba a helyek lekérdezésekor: " + e.getMessage());
        }

        /*// Ha üres (mert nincs a db-ben), generáljunk dummy helyeket,
        // hogy a SeatSelectionController ne haljon el.
        // EZT KÉSŐBB KI KELL VENNI.
        if (seats.isEmpty()) {
            System.err.println("Nincs hely az adatbázisban, dummy helyek generálása...");
            for (char row = 'A'; row <= 'H'; row++) {
                for (int number = 1; number <= 10; number++) {
                    SeatStatus status = Math.random() < 0.2 ? SeatStatus.TAKEN : SeatStatus.FREE;
                    seats.add(new Seat(String.valueOf(row), number, status));
                }
            }
        }*/

        return seats;
    }
    /**
     * Elmenti a foglalást az adatbázisba ÉS frissíti a helyek státuszát.
     * Ez egy tranzakció: vagy mindkét művelet sikerül, vagy egyik sem.
     *
     * @param vetitesId A vetítés egyedi azonosítója.
     * @param selectedSeats A kiválasztott Seat objektumok listája.
     * @param totalPrice A foglalás teljes ára.
     * @return true, ha a foglalás sikeres volt, false egyébként.
     */
    public boolean saveBooking(int vetitesId, List<Seat> selectedSeats, int totalPrice) {
        // SQL parancs a 'Foglalasok' táblába való beszúráshoz [cite: 90]
        String insertBookingSql = "INSERT INTO Foglalasok(vetites_id, ules, ar, foglalas_datuma) VALUES(?,?,?,?)";

        // SQL parancs a 'Helyek' tábla frissítéséhez
        String updateSeatsSql = "UPDATE Helyek SET statusz = 'TAKEN' WHERE vetites_id = ? AND sor = ? AND szam = ?";

        Connection conn = this.connect();

        try {
            // ----- Tranzakció indítása -----
            conn.setAutoCommit(false);

            // 1. Foglalás beszúrása
            try (PreparedStatement pstmtBooking = conn.prepareStatement(insertBookingSql)) {

                String seatsStr = selectedSeats.stream()
                        .map(Seat::getSeatId)
                        .collect(Collectors.joining(", "));

                pstmtBooking.setInt(1, vetitesId);
                pstmtBooking.setString(2, seatsStr);
                pstmtBooking.setDouble(3, totalPrice);
                pstmtBooking.setString(4, LocalDateTime.now().format(dbFormatter)); // dbFormatter-t feljebb definiáltuk
                pstmtBooking.executeUpdate();
            }

            // 2. Helyek frissítése 'TAKEN' státuszra (Batch update)
            try (PreparedStatement pstmtUpdateSeats = conn.prepareStatement(updateSeatsSql)) {
                for (Seat seat : selectedSeats) {
                    pstmtUpdateSeats.setInt(1, vetitesId);
                    pstmtUpdateSeats.setString(2, seat.getRow());
                    pstmtUpdateSeats.setInt(3, seat.getNumber());
                    pstmtUpdateSeats.addBatch(); // Hozzáadás a köteghez
                }
                pstmtUpdateSeats.executeBatch(); // Köteg végrehajtása
            }

            // ----- Tranzakció véglegesítése (Commit) -----
            conn.commit();
            return true; // Siker

        } catch (SQLException e) {
            System.err.println("Hiba a foglalás mentésekor, tranzakció visszavonva: " + e.getMessage());
            // ----- Hiba esetén visszavonás (Rollback) -----
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Hiba a rollback közben: " + ex.getMessage());
            }
            return false; // Sikertelenség

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Visszaállás normál módba
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Hiba a kapcsolat lezárásakor: " + e.getMessage());
            }
        }
    }



}
