package com.example.eventfinder.models;

import java.util.List;

// ========== EventEmbedded.java ==========
public class EventEmbedded {
    private List<Venue> venues;
    private List<Attraction> attractions;

    public List<Venue> getVenues() { return venues; }
    public void setVenues(List<Venue> venues) { this.venues = venues; }

    public List<Attraction> getAttractions() { return attractions; }
    public void setAttractions(List<Attraction> attractions) {
        this.attractions = attractions;
    }
}
