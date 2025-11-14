package com.MoziCity.mozi_jegyfoglalo.db;

import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;

import java.sql.*; // Importáljuk a Statement-et is
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

    /**
     * Létrehozza az adatbázis sémát (táblákat), ha azok még nem léteznek,
     * és feltölti őket alapértelmezett adatokkal.
     */
    public void setupDatabase() {
        // A 'Filmek' tábla a PDF és a Movie modell alapján
        String createFilmekTable = "CREATE TABLE IF NOT EXISTS Filmek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cim TEXT NOT NULL," +
                "leiras TEXT," +
                "imageUrl TEXT" + // Ez a te Movie modelledhez kell
                ");";

        // A 'Vetitesek' tábla a PDF és a Movie modell alapján
        String createVetitesekTable = "CREATE TABLE IF NOT EXISTS Vetitesek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "film_id INTEGER NOT NULL," +
                "datum TEXT NOT NULL," + // Pl. "2025-11-20"
                "idopont TEXT NOT NULL," + // Pl. "18:00"
                "jegyar REAL NOT NULL," + // Az ár a vetítéshez tartozik
                "FOREIGN KEY(film_id) REFERENCES Filmek(id)" +
                ");";

        // === ÚJ TÁBLA ===
        // A 'Felhasznalok' tábla a nevek és e-mailek tárolására
        String createFelhasznalokTable = "CREATE TABLE IF NOT EXISTS Felhasznalok (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nev TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE" + // Az e-mail egyedi
                ");";

        // A 'Helyek' tábla (ezt mi találtuk ki, de logikailag szükséges)
        String createHelyekTable = "CREATE TABLE IF NOT EXISTS Helyek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "vetites_id INTEGER NOT NULL," +
                "sor TEXT NOT NULL," + // 'A', 'B', 'C' ...
                "szam INTEGER NOT NULL," + // 1, 2, 3 ...
                "statusz TEXT NOT NULL DEFAULT 'FREE'," + // FREE, TAKEN
                "FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id)" +
                ");";

        // === MÓDOSÍTOTT TÁBLA ===
        // A 'Foglalasok' tábla most már helyesen hivatkozik a Felhasznalok-ra
        String createFoglalasokTable = "CREATE TABLE IF NOT EXISTS Foglalasok (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "vetites_id INTEGER NOT NULL," +
                "felhasznalo_id INTEGER NOT NULL," + // <-- EZ MÁR NOT NULL
                "ules TEXT NOT NULL," + // Pl. "A1, A2, A3"
                "ar REAL NOT NULL," +
                "foglalas_datuma TEXT NOT NULL," +
                "FOREIGN KEY(vetites_id) REFERENCES Vetitesek(id)," +
                "FOREIGN KEY(felhasznalo_id) REFERENCES Felhasznalok(id)" + // <-- ÚJ HIVATKOZÁS
                ");";

        try (Connection conn = this.connect();
             java.sql.Statement stmt = conn.createStatement()) {

            // Táblák létrehozása
            stmt.execute(createFilmekTable);
            stmt.execute(createVetitesekTable);
            stmt.execute(createFelhasznalokTable); // <-- Új tábla létrehozása
            stmt.execute(createHelyekTable);
            stmt.execute(createFoglalasokTable);

            // ----- ADATOK FELTÖLTÉSE (CSAK HA ÜRES) -----
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Filmek");
            if (rs.getInt(1) == 0) {
                System.out.println("Adatbázis inicializálása dummy adatokkal...");
                populateDummyData(conn);
            }

        } catch (SQLException e) {
            System.err.println("Hiba az adatbázis setup közben: " + e.getMessage());
        }
    }

    /**
     * Feltölti az adatbázist próba adatokkal.
     * (VÁLTOZATLAN)
     */
    private void populateDummyData(Connection conn) throws SQLException {
        // ... (Ez a metódus változatlan marad) ...
        try (PreparedStatement pstmtFilm = conn.prepareStatement("INSERT INTO Filmek (cim, leiras, imageUrl) VALUES (?,?,?)", new String[]{"id"});
             PreparedStatement pstmtVetites = conn.prepareStatement("INSERT INTO Vetitesek (film_id, datum, idopont, jegyar) VALUES (?,?,?,?)", new String[]{"id"});
             PreparedStatement pstmtHely = conn.prepareStatement("INSERT INTO Helyek (vetites_id, sor, szam, statusz) VALUES (?,?,?,?)")) {

            conn.setAutoCommit(false);

            // === Borat ===
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

            // === K-pop démonvadászok ===
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

            // === Barbie ===
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
            System.err.println("Hiba a dummy adatok betöltésekor: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Segédmetódus a frissen generált ID lekéréséhez (VÁLTOZATLAN) */
    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        // ... (Változatlan) ...
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Nem sikerült ID-t generálni.");
            }
        }
    }

    /** Segédmetódus, legenerálja az ülőhelyeket (VÁLTOZATLAN) */
    private void generateSeatsForShow(PreparedStatement pstmtHely, int vetitesId) throws SQLException {
        // ... (Változatlan) ...
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

    /**
     * Lekérdezi az összes filmet (VÁLTOZATLAN)
     */
    public List<Movie> getAllMovies() {
        // ... (Ez a metódus változatlan marad) ...
        String sql = "SELECT v.id as vetites_id, f.cim, f.leiras, f.imageUrl, v.datum, v.idopont, v.jegyar " +
                "FROM Filmek f " +
                "JOIN Vetitesek v ON f.id = v.film_id";
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

    /**
     * Lekérdezi egy adott vetítés helyeit. (VÁLTOZATLAN)
     */
    public List<Seat> getSeatsForShow(int vetitesId) {
        // ... (Ez a metódus változatlan marad) ...
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

    /**
     * Elmenti a foglalást az adatbázisba ÉS frissíti a helyek státuszát.
     * EZ A METÓDUS MÁR HELYESEN MŰKÖDIK A NÉVVEL ÉS E-MAILLEL.
     *
     * @param vetitesId A vetítés egyedi azonosítója.
     * @param selectedSeats A kiválasztott Seat objektumok listája.
     * @param totalPrice A foglalás teljes ára.
     * @param customerName A felhasználó neve.
     * @param customerEmail A felhasználó e-mail címe.
     * @return true, ha a foglalás sikeres volt, false egyébként.
     */
    public boolean saveBooking(int vetitesId, List<Seat> selectedSeats, int totalPrice, String customerName, String customerEmail) {

        // === MÓDOSÍTOTT SQL ===
        // Most már a felhasznalo_id-t is beillesztjük
        String insertBookingSql = "INSERT INTO Foglalasok(vetites_id, felhasznalo_id, ules, ar, foglalas_datuma) VALUES(?,?,?,?,?)";
        String updateSeatsSql = "UPDATE Helyek SET statusz = 'TAKEN' WHERE vetites_id = ? AND sor = ? AND szam = ?";

        Connection conn = this.connect();

        try {
            conn.setAutoCommit(false);

            // === 1. LÉPÉS (ÚJ): Felhasználó ID lekérése vagy létrehozása ===
            int felhasznaloId = getOrCreateUser(conn, customerName, customerEmail);

            // === 2. LÉPÉS: Foglalás beszúrása (MÓDOSÍTVA) ===
            try (PreparedStatement pstmtBooking = conn.prepareStatement(insertBookingSql)) {

                String seatsStr = selectedSeats.stream()
                        .map(Seat::getSeatId)
                        .collect(Collectors.joining(", "));

                pstmtBooking.setInt(1, vetitesId);
                pstmtBooking.setInt(2, felhasznaloId); // <-- EZ AZ ÚJ PARAMÉTER
                pstmtBooking.setString(3, seatsStr);
                pstmtBooking.setDouble(4, totalPrice);
                pstmtBooking.setString(5, LocalDateTime.now().format(dbFormatter));
                pstmtBooking.executeUpdate();
            }

            // === 3. LÉPÉS: Helyek frissítése (VÁLTOZATLAN) ===
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
            return true; // Siker

        } catch (SQLException e) {
            System.err.println("Hiba a foglalás mentésekor, tranzakció visszavonva: " + e.getMessage());
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
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Hiba a kapcsolat lezárásakor: " + e.getMessage());
            }
        }
    }


    /**
     * === ÚJ SEGÉDFÜGGVÉNY ===
     * Megkeres egy felhasználót e-mail alapján, vagy létrehozza, ha nem létezik.
     * @param conn Aktív adatbázis-kapcsolat (tranzakción belül!)
     * @param name A felhasználó neve
     * @param email A felhasználó e-mail címe
     * @return A felhasználó (meglévő vagy új) ID-ja
     * @throws SQLException Hiba esetén kivételt dob
     */
    private int getOrCreateUser(Connection conn, String name, String email) throws SQLException {
        // 1. Megpróbáljuk lekérdezni az e-mail alapján
        String selectSql = "SELECT id FROM Felhasznalok WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Ha megvan, visszaadjuk az ID-ját
                return rs.getInt("id");
            }
        }

        // 2. Ha nem találtuk meg, létrehozzuk az új felhasználót
        String insertSql = "INSERT INTO Felhasznalok(nev, email) VALUES(?, ?)";
        // A Statement.RETURN_GENERATED_KEYS kell, hogy visszakapjuk az új ID-t
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();

            // Kérjük el az auto-generált ID-t
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Visszaadjuk az új ID-t
                } else {
                    throw new SQLException("Nem sikerült létrehozni a felhasználót, nincs ID.");
                }
            }
        }
    }
}