package com.MoziCity.mozi_jegyfoglalo.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// FONTOS: Statikus importok a Mockito-hoz
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import javafx.application.Platform;
import javafx.scene.control.Label;    // <--- EZT NE FELEJTSD EL!
import javafx.scene.control.TextField;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.MoziCity.mozi_jegyfoglalo.MainApp;
import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import com.MoziCity.mozi_jegyfoglalo.model.Movie;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;

@ExtendWith(MockitoExtension.class)
public class ConfirmationControllerTest {

    @Mock
    private MainApp mainApp;

    @Mock
    private DatabaseManager dbManager;

    @InjectMocks
    private ConfirmationController controller;

    private TextField nameTextField;
    private TextField emailTextField;

    @BeforeAll
    static void initJfx() {
        // JavaFX Toolkit inicializálása (hogy a Label/TextField létrehozható legyen)
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Létrehozzuk a JavaFX elemeket (Mockolás helyett igazi példányok)
        nameTextField = new TextField();
        emailTextField = new TextField();

        // --- EZ A RÉSZ HIÁNYZOTT, EZÉRT VOLT NULLPOINTEREXCEPTION ---
        // Létrehozunk "kamu" címkéket, hogy a kontroller tudjon rájuk írni
        Label movieTitleLabel = new Label();
        Label showtimeLabel = new Label();
        Label seatsLabel = new Label();
        Label totalPriceLabel = new Label();

        // 2. Beinjektáljuk őket a kontroller privát mezőibe (Reflexióval)
        setPrivateField(controller, "nameTextField", nameTextField);
        setPrivateField(controller, "emailTextField", emailTextField);

        // --- A LABEL-EKET IS BE KELL INJEKTÁLNI ---
        setPrivateField(controller, "movieTitleLabel", movieTitleLabel);
        setPrivateField(controller, "showtimeLabel", showtimeLabel);
        setPrivateField(controller, "seatsLabel", seatsLabel);
        setPrivateField(controller, "totalPriceLabel", totalPriceLabel);

        // 3. Adatbázis és adatok beállítása
        controller.setDbManager(dbManager);

        Movie movie = new Movie(1, "Teszt Film", "url", "leiras", LocalDateTime.now(), 2000);
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("A", 1, SeatStatus.SELECTED));

        // Most már nem fog elszállni, mert a Labe-ek léteznek!
        controller.setBookingDetails(movie, seats);
    }

    @Test
    void testHandleConfirm_SuccessfulBooking() {
        // Given (Előkészítés)
        nameTextField.setText("Teszt Elek");
        emailTextField.setText("teszt@elek.hu");

        when(dbManager.saveBooking(anyInt(), anyList(), anyInt(), anyString(), anyString()))
                .thenReturn(true);

        // When (Cselekvés)
        controller.handleConfirm();

        // Then (Ellenőrzés)
        verify(mainApp).showInfo(eq("Sikeres foglalás"), contains("Köszönjük"));
        verify(mainApp).showMovieSelectionScene();
    }

    @Test
    void testHandleConfirm_MissingName_ShowError() {
        // Given
        nameTextField.setText(""); // Üres név
        emailTextField.setText("teszt@elek.hu");

        // When
        controller.handleConfirm();

        // Then
        verify(mainApp).showError(eq("Hiányzó adat"), anyString());
        // Biztosítjuk, hogy NEM hívta meg a mentést
        verify(dbManager, never()).saveBooking(anyInt(), anyList(), anyInt(), anyString(), anyString());
    }

    // Segédmetódus a privát mezők eléréséhez
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}