package com.MoziCity.mozi_jegyfoglalo.service;

import com.MoziCity.mozi_jegyfoglalo.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    private final List<CartItem> cartItems = new ArrayList<>();

    public void addToCart(CartItem item) {
        cartItems.add(item);
    }

    public void removeFromCart(String movieTitle, String seatNumber) {
        cartItems.removeIf(item ->
                item.getMovieTitle().equals(movieTitle)
                        && item.getSeatNumber().equals(seatNumber)
        );
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice();
        }
        return total;
    }

    public int getItemCount() {
        return cartItems.size();
    }
}

