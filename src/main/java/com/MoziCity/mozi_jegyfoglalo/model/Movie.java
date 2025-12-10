package com.MoziCity.mozi_jegyfoglalo.model;

import java.time.LocalDateTime;

public class Movie {
    private final int vetitesId;
    private final String title;
    private final String imageUrl;
    private final String description;
    private final LocalDateTime showtime;
    private final int price;

    // --- MÓDOSÍTÁS: A konstruktort is frissítjük ---
    public Movie(int vetitesId, String title, String imageUrl, String description, LocalDateTime showtime, int price) {
        this.vetitesId = vetitesId; // Ezt is beállítjuk
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.showtime = showtime;
        this.price = price;
    }

    public int getVetitesId() { return vetitesId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public LocalDateTime getShowtime() { return showtime; }
    public int getPrice() { return price; }
}