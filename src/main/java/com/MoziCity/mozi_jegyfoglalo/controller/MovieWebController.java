package com.MoziCity.mozi_jegyfoglalo.controller;

import com.MoziCity.mozi_jegyfoglalo.db.DatabaseManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MovieWebController {

    private final DatabaseManager dbManager;

    public MovieWebController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // --- FŐOLDAL ---
    @GetMapping("/")
    public String listMovies(Model model, HttpSession session) {
        model.addAttribute("filmek", dbManager.getAllMovies());
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);
        return "index";
    }

    // --- ADMIN BELÉPÉS ---
    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String password, HttpSession session) {
        if ("admin123".equals(password)) {
            session.setAttribute("isAdmin", true);
        }
        return "redirect:/";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("isAdmin");
        return "redirect:/";
    }

    // --- TÖRLÉS ---
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable int id, HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) {
            dbManager.deleteShowtime(id);
        }
        return "redirect:/";
    }

    // --- HOZZÁADÁS ---
    @GetMapping("/add-movie")
    public String showAddMovieForm(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) return "redirect:/";
        return "add-movie"; // HTML űrlap betöltése
    }

    @PostMapping("/add-movie")
    public String processAddMovie(@RequestParam String title, @RequestParam String description,
                                  @RequestParam String imageUrl, @RequestParam String date,
                                  @RequestParam String time, @RequestParam int price,
                                  HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) {
            // JAVÍTÁS: Itt a helyes metódusnevet használjuk most már!
            dbManager.addNewMovieAndShowtime(title, description, imageUrl, date, time, price);
        }
        return "redirect:/";
    }
}