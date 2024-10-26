package com.example.iot_lab5_20212607.model;

public class MotivationalPhrase {
    private long id;
    private String phrase;

    public MotivationalPhrase(long id, String phrase) {
        this.id = id;
        this.phrase = phrase;
    }

    // Constructor sin ID para nuevas frases
    public MotivationalPhrase(String phrase) {
        this.phrase = phrase;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getPhrase() { return phrase; }
    public void setPhrase(String phrase) { this.phrase = phrase; }
}
