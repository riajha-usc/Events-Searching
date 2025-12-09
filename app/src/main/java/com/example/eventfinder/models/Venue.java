package com.example.eventfinder.models;

import java.util.List;

public class Venue {
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
