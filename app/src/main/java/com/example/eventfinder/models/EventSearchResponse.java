package com.example.eventfinder.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventSearchResponse {
    @SerializedName("_embedded")
    private Embedded embedded;

    public Embedded getEmbedded() { return embedded; }
    public void setEmbedded(Embedded embedded) { this.embedded = embedded; }

    public static class Embedded {
        private List<Event> events;

        public List<Event> getEvents() { return events; }
        public void setEvents(List<Event> events) { this.events = events; }
    }
}