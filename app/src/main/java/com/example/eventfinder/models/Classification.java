package com.example.eventfinder.models;

// ========== Classification.java ==========
public class Classification {
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
