package com.example.eventfinder.models;

public class FavoriteResponse {
    private String message;
    private boolean isFavorite;
    private FavoriteEvent data;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public FavoriteEvent getData() { return data; }
    public void setData(FavoriteEvent data) { this.data = data; }
}