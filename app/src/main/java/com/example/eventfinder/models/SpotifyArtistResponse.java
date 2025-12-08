package com.example.eventfinder.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyArtistResponse {
    private Artists artists;

    public Artists getArtists() { return artists; }
    public void setArtists(Artists artists) { this.artists = artists; }

    public static class Artists {
        private List<Artist> items;

        public List<Artist> getItems() { return items; }
        public void setItems(List<Artist> items) { this.items = items; }
    }

    public static class Artist {
        private String id;
        private String name;
        private List<Image> images;
        private Followers followers;
        private int popularity;
        private List<String> genres;

        @SerializedName("external_urls")
        private ExternalUrls externalUrls;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<Image> getImages() { return images; }
        public void setImages(List<Image> images) { this.images = images; }

        public Followers getFollowers() { return followers; }
        public void setFollowers(Followers followers) { this.followers = followers; }

        public int getPopularity() { return popularity; }
        public void setPopularity(int popularity) { this.popularity = popularity; }

        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }

        public ExternalUrls getExternalUrls() { return externalUrls; }
        public void setExternalUrls(ExternalUrls externalUrls) {
            this.externalUrls = externalUrls;
        }
    }

    public static class Image {
        private String url;
        private int width;
        private int height;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }

    public static class Followers {
        private int total;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }

    public static class ExternalUrls {
        private String spotify;

        public String getSpotify() { return spotify; }
        public void setSpotify(String spotify) { this.spotify = spotify; }
    }
}