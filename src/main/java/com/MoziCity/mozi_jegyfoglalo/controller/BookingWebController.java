//EZ helyettesíti a SeatSelectionController-t és a ConfirmationController-t.

package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import com.MoziCity.mozi_jegyfoglalo.model.Seat;
import com.MoziCity.mozi_jegyfoglalo.model.SeatStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookingWebController {

    private final DatabaseManager dbManager;

    public BookingWebController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // 1. HELYVÁLASZTÁS MEGJELENÍTÉSE
    @GetMapping("/foglalas")
    public String showSeatSelection(@RequestParam("id") int vetitesId, Model model) {
        // Lekérjük a helyeket (mint a SeatSelectionController-ben)
        List<Seat> seats = dbManager.getSeatsForShow(vetitesId);

        // Lekérjük a film adatait (csak a cím/időpont miatt kellhet, egyszerűsítve)
        // A valóságban itt lekérhetnéd a Movie objektumot is a vetitesId alapján.

        model.addAttribute("seats", seats);
        model.addAttribute("vetitesId", vetitesId);
        return "seat-selection";
    }

    // 2. MEGERŐSÍTÉS ELŐKÉSZÍTÉSE (A "Tovább" gomb után)
    @PostMapping("/megerosites")
    public String showConfirmation(@RequestParam("vetitesId") int vetitesId,
                                   @RequestParam(value = "selectedSeats", required = false) List<String> selectedSeatIds,
                                   Model model) {

        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            return "redirect:/foglalas?id=" + vetitesId; // Ha nem választott semmit, vissza
        }

        // Visszaalakítjuk a String ID-kat (pl. "A1") Seat objektumokká
        // (Egyszerűsítve: újra lekérjük a DB-ből és szűrjük)
        List<Seat> allSeats = dbManager.getSeatsForShow(vetitesId);
        List<Seat> chosenSeats = allSeats.stream()
                .filter(s -> selectedSeatIds.contains(s.getRow() + s.getNumber()))
                .collect(Collectors.toList());

        // Ár kiszámítása (Tegyük fel, hogy fix, vagy lekérhetnéd a DB-ből)
        int pricePerTicket = 2500; // Ezt a DB-ből kéne venni a Movie objectből
        int totalPrice = chosenSeats.size() * pricePerTicket;

        model.addAttribute("vetitesId", vetitesId);
        model.addAttribute("chosenSeats", chosenSeats);
        model.addAttribute("totalPrice", totalPrice);

        // A választott helyeket vesszővel elválasztva átadjuk a következő lépésnek
        String seatString = String.join(",", selectedSeatIds);
        model.addAttribute("seatString", seatString);

        return "confirmation";
    }

    // 3. VÉGLEGESÍTÉS (Mentés a DB-be)
    @PostMapping("/mentes")
    public String saveBooking(@RequestParam("vetitesId") int vetitesId,
                              @RequestParam("seatString") String seatString,
                              @RequestParam("totalPrice") int totalPrice,
                              @RequestParam("name") String name,
                              @RequestParam("email") String email,
                              Model model) {

        // Visszaalakítjuk a seatString-et Seat listává a mentéshez
        List<Seat> selectedSeats = new ArrayList<>();
        String[] ids = seatString.split(",");
        for (String id : ids) {
            String row = id.substring(0, 1);
            int num = Integer.parseInt(id.substring(1));
            selectedSeats.add(new Seat(row, num, SeatStatus.SELECTED));
        }

        // Hívjuk a már megírt, profi mentési logikát
        boolean success = dbManager.saveBooking(vetitesId, selectedSeats, totalPrice, name, email);

        if (success) {
            return "success"; // Sikeres oldal
        } else {
            return "redirect:/"; // Hiba esetén főoldal (vagy hibaoldal)
        }
    }
}