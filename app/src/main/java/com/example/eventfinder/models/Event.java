package com.example.eventfinder.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// ========== Event.java ==========
public class Event {
    private String id;
    private String name;
    private String url;

    @SerializedName("images")
    private List<EventImage> images;

    @SerializedName("dates")
    private EventDates dates;

    @SerializedName("classifications")
    private List<Classification> classifications;

    @SerializedName("_embedded")
    private EventEmbedded embedded;

    @SerializedName("priceRanges")
    private List<PriceRange> priceRanges;

    @SerializedName("seatmap")
    private SeatMap seatmap;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<EventImage> getImages() { return images; }
    public void setImages(List<EventImage> images) { this.images = images; }

    public EventDates getDates() { return dates; }
    public void setDates(EventDates dates) { this.dates = dates; }

    public List<Classification> getClassifications() { return classifications; }
    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }

    public EventEmbedded getEmbedded() { return embedded; }
    public void setEmbedded(EventEmbedded embedded) { this.embedded = embedded; }

    public List<PriceRange> getPriceRanges() { return priceRanges; }
    public void setPriceRanges(List<PriceRange> priceRanges) {
        this.priceRanges = priceRanges;
    }

    public SeatMap getSeatmap() { return seatmap; }
    public void setSeatmap(SeatMap seatmap) { this.seatmap = seatmap; }
}

