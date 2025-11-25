package com.MoziCity.mozi_jegyfoglalo.model;

public class CartItem {
    private String movieTitle;
    private String seatNumber;
    private double price;

    public CartItem(String movieTitle, String seatNumber, double price) {
        this.movieTitle = movieTitle;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

