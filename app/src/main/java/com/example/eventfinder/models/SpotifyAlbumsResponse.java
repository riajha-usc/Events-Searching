package com.example.eventfinder.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyAlbumsResponse {
    private List<Album> items;

    public List<Album> getItems() { return items; }
    public void setItems(List<Album> items) { this.items = items; }

    public static class Album {
        private String id;
        private String name;

        @SerializedName("release_date")
        private String releaseDate;

        private List<SpotifyArtistResponse.Image> images;

        @SerializedName("external_urls")
        private SpotifyArtistResponse.ExternalUrls externalUrls;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

        public List<SpotifyArtistResponse.Image> getImages() { return images; }
        public void setImages(List<SpotifyArtistResponse.Image> images) {
            this.images = images;
        }

        public SpotifyArtistResponse.ExternalUrls getExternalUrls() {
            return externalUrls;
        }
        public void setExternalUrls(SpotifyArtistResponse.ExternalUrls externalUrls) {
            this.externalUrls = externalUrls;
        }
    }
}