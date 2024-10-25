package com.example.iot_lab5_20212607.model;

public class Meal {
    private long id;
    private String name;
    private String time;
    private int calories;
    private String date;


    public Meal(String name, String time, int calories, String date) {
        this.name = name;
        this.time = time;
        this.calories = calories;
        this.date = date;
    }


    public Meal(long id, String name, String time, int calories, String date) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.calories = calories;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getCalories() {
        return calories;
    }

    public String getDate() {
        return date;
    }
}

