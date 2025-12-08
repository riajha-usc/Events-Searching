package com.example.eventfinder.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SuggestResponse {
    @SerializedName("_embedded")
    private Embedded embedded;

    public Embedded getEmbedded() { return embedded; }
    public void setEmbedded(Embedded embedded) { this.embedded = embedded; }

    public static class Embedded {
        private List<Attraction> attractions;

        public List<Attraction> getAttractions() { return attractions; }
        public void setAttractions(List<Attraction> attractions) {
            this.attractions = attractions;
        }
    }

    public static class Attraction {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}