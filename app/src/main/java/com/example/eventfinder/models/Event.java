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

// ========== EventImage.java ==========
class EventImage {
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

// ========== EventDates.java ==========
class EventDates {
    private DateInfo start;
    private Status status;

    public DateInfo getStart() { return start; }
    public void setStart(DateInfo start) { this.start = start; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}

class DateInfo {
    private String localDate;
    private String localTime;

    public String getLocalDate() { return localDate; }
    public void setLocalDate(String localDate) { this.localDate = localDate; }

    public String getLocalTime() { return localTime; }
    public void setLocalTime(String localTime) { this.localTime = localTime; }
}

class Status {
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

// ========== Classification.java ==========
class Classification {
    private Segment segment;
    private Genre genre;
    private SubGenre subGenre;
    private Type type;
    private SubType subType;

    public Segment getSegment() { return segment; }
    public void setSegment(Segment segment) { this.segment = segment; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public SubGenre getSubGenre() { return subGenre; }
    public void setSubGenre(SubGenre subGenre) { this.subGenre = subGenre; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public SubType getSubType() { return subType; }
    public void setSubType(SubType subType) { this.subType = subType; }
}

class Segment {
    private String id;
    private String name;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

class Genre {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

class SubGenre {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

class Type {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

class SubType {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// ========== EventEmbedded.java ==========
class EventEmbedded {
    private List<Venue> venues;
    private List<Attraction> attractions;

    public List<Venue> getVenues() { return venues; }
    public void setVenues(List<Venue> venues) { this.venues = venues; }

    public List<Attraction> getAttractions() { return attractions; }
    public void setAttractions(List<Attraction> attractions) {
        this.attractions = attractions;
    }
}

class Venue {
    private String name;
    private String url;
    private Address address;
    private List<EventImage> images;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public List<EventImage> getImages() { return images; }
    public void setImages(List<EventImage> images) { this.images = images; }
}

class Address {
    private String line1;

    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
}

class Attraction {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// ========== PriceRange.java ==========
class PriceRange {
    private double min;
    private double max;

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
}

// ========== SeatMap.java ==========
class SeatMap {
    private String staticUrl;

    public String getStaticUrl() { return staticUrl; }
    public void setStaticUrl(String staticUrl) { this.staticUrl = staticUrl; }
}