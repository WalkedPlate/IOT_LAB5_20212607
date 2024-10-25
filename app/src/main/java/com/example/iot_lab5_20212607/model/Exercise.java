package com.example.iot_lab5_20212607.model;

public class Exercise {
    private long id;
    private String name;
    private int duration;
    private int caloriesBurned;
    private String time;
    private String date;

    // Constructor para crear nuevos ejercicios
    public Exercise(String name, int duration, int caloriesBurned, String time, String date) {
        this.name = name;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.time = time;
        this.date = date;
    }

    // Constructor para ejercicios desde la base de datos
    public Exercise(long id, String name, int duration, int caloriesBurned, String time, String date) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.time = time;
        this.date = date;
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public int getDuration() { return duration; }
    public int getCaloriesBurned() { return caloriesBurned; }
    public String getTime() { return time; }
    public String getDate() { return date; }
}
