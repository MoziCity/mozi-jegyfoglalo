package com.MoziCity.mozi_jegyfoglalo.model;

public class Seat {
    private final String row;
    private final int number;
    private SeatStatus status;

    public Seat(String row, int number, SeatStatus status) {
        this.row = row;
        this.number = number;
        this.status = status;
    }

    public String getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getSeatId() {
        return row + number;
    }
}
